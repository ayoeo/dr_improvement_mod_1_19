package com.twoandahalfdevs.dr_improvement_mod

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import org.joml.Math
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

var latestExp: Float = 0f
var prevExp: Float = 0f
var prevSlot: Int = 0
var expUpdateTime: Long = 0L
var clas = "???"
var actionBarMsg: String? = ""
var actionBarTime = 0
private var cdActive = false
private var cd: Int? = null
private var lastUpdatedCdTime = System.currentTimeMillis()
private var lastUpdatedPotCdTime = System.currentTimeMillis()

// Might need to rollback if we get a reflect in there
var rollbackLastUpdatedBonusTime = System.currentTimeMillis()
var rollbackLastUpdatedCombatTime = System.currentTimeMillis()
var rollbackBonusTimer = 0.0
var rollbackCombatTimer = 0.0

// Combat tracking stuff
var lastUpdatedBonusTime = System.currentTimeMillis()
  set(value) {
    rollbackLastUpdatedBonusTime = field
    field = value
  }

var lastUpdatedCombatTime = System.currentTimeMillis()
  set(value) {
    rollbackLastUpdatedCombatTime = field
    field = value
  }

var bonusTimer = 0.0
  set(value) {
    rollbackBonusTimer = field
    field = value
  }

var combatTimer = 0.0
  set(value) {
    rollbackCombatTimer = field
    field = value
  }

private fun rollBackCombatTimers() {
  lastUpdatedBonusTime = rollbackLastUpdatedBonusTime
  lastUpdatedCombatTime = rollbackLastUpdatedCombatTime
  bonusTimer = rollbackBonusTimer
  combatTimer = rollbackCombatTimer
}

private var pots = 5
private var potCd: Int? = null
private var totalPots = 0

private val ultCdReg =
  """§(.)(?:The Fast|Berserk|Divine Protection|Deaths Grasp): \[(?:([0-9]*)m)? ?(?:([0-9]*)s)?]""".toRegex()
private val potReg = """\[([0-9]*)/5] Potions: \[([0-9]*)s]""".toRegex()

//private val rankReg = """S|S\+|S\+\+|GD|QA|LORE|GM|PMOD|DEV|OWNER""".toRegex()
private val guildReg = """\[.+]""".toRegex()
private val guildTagReg = """\[(.+)] .+""".toRegex()

private val abilityReg = """(.*) has activated The Fast""".toRegex()
private val debugDmg = """[0-9]+ \S*DMG -> (.+) \[[0-9]+ HP]|-[0-9]+ \S*HP \((.+)\)""".toRegex()
private val notRealCombat = listOf("FALL")
private val reflectReg = """\*\s+(OPPONENT\s+)?REFLECTED.*\[\d+]""".toRegex()

private val anyActiveStartReg = """(.*) has activated (The Fast|Berserk|Divine Protection|Death's Grasp)""".toRegex()

private val combatBonusTime
  get() = 4 + 0.5 * ModConfig.settings.justMyNaturePoints

var scoreWasUpdated: MutableMap<String, Int> = ConcurrentHashMap()
var latestCurrentHealth: MutableMap<String, Float> = ConcurrentHashMap()
var maxHealthValues: MutableMap<String, Int> = ConcurrentHashMap()

val playerCdMap = hashMapOf<AbstractClientPlayerEntity, Pair<String, Long>>()

private const val BASE_PVE = 8.0
private val combatPvETime: Double
  get() = if (clas.contains("Rogue")) {
    val shadowmeldMult = 1.0 - ModConfig.settings.shadowmeldPoints * 0.035
    BASE_PVE * shadowmeldMult
  } else {
    BASE_PVE
  }

private const val BASE_PVP = 15.0
private val combatPvPTime: Double
  get() = if (clas.contains("Rogue")) {
    val shadowmeldMult = 1.0 - ModConfig.settings.shadowmeldPoints * 0.035
    BASE_PVP * shadowmeldMult
  } else {
    BASE_PVP
  }

fun interpolatedExp(): Float {
  val expDelta: Float = latestExp - prevExp

  val deltaTimeMs: Float = (System.nanoTime() - expUpdateTime) / 1000000f
  // If we're going up, interpolate slower
  return if (expDelta > 0f) {
    prevExp + expDelta * Math.clamp(0f, 1f, deltaTimeMs / 50f)
  } else {
    prevExp + expDelta * Math.clamp(0f, 1f, deltaTimeMs / 10f)
  }
}

class DrImprovementMod : ModInitializer, ClientTickEvents.StartTick {
  override fun onStartTick(client: MinecraftClient) {
    if (client.world != null) {
      // Update health
      val toRemove = HashSet<String?>()
      for (entry in scoreWasUpdated.entries) {
        val player =
          client.world!!.getPlayers().stream().filter { p: AbstractClientPlayerEntity? -> p!!.entityName == entry.key }
            .findFirst()
        val health: Float? = latestCurrentHealth.getOrDefault(entry.key, null)
        if (player.isPresent && health != null) {
          val ratio = player.get().maxHealth / health
          val maxHealth = entry.value.toDouble() * ratio
          if (!java.lang.Double.isNaN(maxHealth)) {
            maxHealthValues.put(player.get().entityName, Math.round(maxHealth).toInt())
          }
          toRemove.add(entry.key)
        }
      }

      for (e in toRemove) {
        scoreWasUpdated.remove(e)
      }

      // Update the scoreboard to reflect real health values
      val scoreboard = client.world!!.scoreboard
      val healthObjective = scoreboard.getObjective("health") ?: return
      val scores = scoreboard.getAllPlayerScores(healthObjective)
      for (score in scores) {
        val maxHealth: Int? = maxHealthValues[score.playerName]
        if (maxHealth != null) {
          val player = client.world!!.getPlayers().stream()
            .filter { p: AbstractClientPlayerEntity? -> p!!.entityName == score.playerName }.findFirst()

          if (player.isPresent) {
            val ratio = player.get().health / player.get().maxHealth
            score.score = Math.round(maxHealth.toDouble() * ratio).toInt()
          }
        }
      }
    }
  }

  override fun onInitialize() {
    ClientTickEvents.START_CLIENT_TICK.register(this)
    registerHudEvent()
    ModConfig.load()
  }

  fun registerHudEvent() {
    HudRenderCallback.EVENT.register(HudRenderCallback { matrices: MatrixStack, tickDelta: Float ->
      if (!ModConfig.settings.usefulInfo) return@HudRenderCallback

      val minecraft = MinecraftClient.getInstance()

      // Cooldown
      if (actionBarTime > 0 && actionBarMsg != null) {
        val cdMatches = ultCdReg.find(actionBarMsg!!)
        if (cdMatches != null) {
          try {
            val col = cdMatches.groupValues.getOrNull(1)
            val min = cdMatches.groupValues.getOrNull(2)
            val sec = cdMatches.groupValues.getOrNull(3)
            val minutes = if (min != null && min.isNotEmpty()) min.toInt() else 0
            val seconds = if (sec != null && sec.isNotEmpty()) sec.toInt() else 0
            cd = minutes * 60 + seconds
            cdActive = col == "a"
            lastUpdatedCdTime = System.currentTimeMillis() - ((60 - actionBarTime) * 50)
          } catch (e: NumberFormatException) {
            e.printStackTrace()
          }
        } else {
          cd = null
        }

        val potMatches = potReg.find(actionBarMsg!!)
        if (potMatches != null) {
          pots = potMatches.groupValues.getOrNull(1)?.toInt() ?: 0
          potCd = potMatches.groupValues.getOrNull(2)?.toInt()
          lastUpdatedPotCdTime = System.currentTimeMillis() - ((60 - actionBarTime) * 50)
        } else {
          // Nothing on the bar, no pot cooldown.
          pots = 5
          potCd = null
        }
      } else {
        cd = null
        pots = 5
        potCd = null
      }

      totalPots = minecraft.player!!.inventory.main.count {
        it.item.translationKey.equals("item.minecraft.potion") || it.item.translationKey.equals("item.minecraft.splash_potion")
      }

      val probablyTheCoolDownNow = cd?.let {
        (it - (System.currentTimeMillis() - lastUpdatedCdTime) / 1000)
      }

      val probablyCombatTimer =
        (combatTimer - (System.currentTimeMillis() - lastUpdatedCombatTime) / 1000.0).coerceAtLeast(
          0.0
        )

      val probablyBonusTimer =
        (bonusTimer.toDouble() - (System.currentTimeMillis() - lastUpdatedBonusTime) / 1000.0).coerceAtLeast(
          0.0
        )

      val cdAboveZero = probablyTheCoolDownNow != null &&
        // cd over zero OR it's been less than a second since our last update
        (probablyTheCoolDownNow > 0 || (System.currentTimeMillis() - lastUpdatedCdTime < 1000))
      val cdStr = if (cdAboveZero) "§${if (cdActive) "a" else "c"}${probablyTheCoolDownNow}s" else "§aReady"

      matrices.push()

      val xCenter = minecraft.framebuffer.viewportWidth / 4
      val yCenter = minecraft.framebuffer.viewportHeight / 4

      val xOffset = 8
      val xOffsetBottom = 12
      val yOffset = 0
      val ySpreadAbilPots = -34
      val ySpreadCombat = 27

      val textColor = 0xB4FFFFFF.toInt()

      minecraft.textRenderer.drawWithShadow(
        matrices,
        cdStr,
        xCenter.toFloat() - minecraft.textRenderer.getWidth(cdStr) - xOffset,
        yCenter.toFloat() - yOffset + ySpreadAbilPots,
        textColor,
      )
      var color = "§c"
      val usablePots = pots.coerceAtMost(totalPots)
      if (usablePots >= 3) color = "§a"
      else if (usablePots >= 1) color = "§e"
      var potsStr = "${color}$usablePots / $totalPots"
      if (potCd != null) {
        val potCdNow = (potCd!! - (System.currentTimeMillis() - lastUpdatedPotCdTime) / 1000).coerceAtLeast(0)
        potsStr += " §7(${potCdNow}s)"
      }
      minecraft.textRenderer.drawWithShadow(
        matrices,
        potsStr,
        xCenter.toFloat() + xOffset,
        yCenter.toFloat() - yOffset + ySpreadAbilPots,
        textColor,
      )

      val combatstr = if (probablyCombatTimer > 0) "§c${
        probablyCombatTimer.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
      }s" else "§a:)"

      val combatstrWidth = minecraft.textRenderer.getWidth(combatstr)
      minecraft.textRenderer.drawWithShadow(
        matrices,
        combatstr,
        xCenter.toFloat() - (if (clas.contains("Rogue")) combatstrWidth + xOffsetBottom else combatstrWidth / 2),
        yCenter.toFloat() - yOffset + ySpreadCombat,
        textColor,
      )

      val bonusStr = if (probablyCombatTimer <= 0) {
        "§a(:"
      } else {
        if (probablyBonusTimer > 0) "§a${
          probablyBonusTimer.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
        }s" else "§c):"
      }

      if (clas.contains("Rogue")) {
        minecraft.textRenderer.drawWithShadow(
          matrices,
          bonusStr,
          xCenter.toFloat() + xOffsetBottom,
          yCenter.toFloat() - yOffset + ySpreadCombat,
          textColor,
        )
      }
      matrices.pop()
    })
  }
}

private fun durAndCdFromAbility(ability: String?) = when (ability) {
  "The Fast" -> Triple(8, 45, 25.65)
  "Berserk" -> Triple(10, 55, 17.6)
  "Divine Protection" -> Triple(10, 55, 29.07)
  "Death's Grasp" -> Triple(6, 50, 28.5)
  else -> null
}

fun guildTag(name: String): String? {
  val tagMatches = guildTagReg.find(name)
  if (tagMatches != null) {
    val tag = tagMatches.groupValues.getOrNull(1)
//    println("tag: $tag")
    return tag
  }
  return null
}

fun cdString(player: AbstractClientPlayerEntity): String? {
  val (abil, activationTime) = playerCdMap.get(player) ?: return null
  val (dur, cdLong, cdShort) = durAndCdFromAbility(abil) ?: return null
  val secsSinceActivation = (System.currentTimeMillis() - activationTime) / 1000.0

  val n = ceil(secsSinceActivation).roundToInt()

  return if (secsSinceActivation < dur) {
    // Still active
    "§a${dur - n}"
  } else if (secsSinceActivation < cdShort) {
    // On cd
    "§c${n}"
  } else if (secsSinceActivation < cdLong) {
    // Might be on cd
    "§e${n}"
  } else {
    // Ready to go
//    "§a▪"
    null
  }
}

fun onChatMessage(msg: String) {
  val minecraft = MinecraftClient.getInstance()

  // If we reflect, we're not really in combat
  val reflectMatches = reflectReg.find(msg)
  if (reflectMatches != null) {
    rollBackCombatTimers()
    return
  }

  val activeStartMatches = anyActiveStartReg.find(msg)
  if (activeStartMatches != null) {
    val name = activeStartMatches.groupValues.getOrNull(1)
    val abil = activeStartMatches.groupValues.getOrNull(2)
    val p = minecraft.world!!.players.find {
      name?.endsWith(it.name.string) == true
    }

    val (dur, cd) = durAndCdFromAbility(abil) ?: return
    if (p != null && abil != null) {
//      println("player activated: ${p?.name}, abil='$abil'")
      playerCdMap.put(p, Pair(abil, System.currentTimeMillis()))
    }
//    println("dur: dur=$dur, cd=$cd")
  }

  val abilMatches = abilityReg.find(msg)
  val matches = abilMatches?.groupValues?.getOrNull(1)
  if (matches?.endsWith(minecraft.player!!.name.string) == true) {
    // Ability takes us out of combat now
    combatTimer = 0.0
  }

  val dmgMatches = debugDmg.find(msg)
  val attacked = dmgMatches?.groupValues?.getOrNull(1)
  val attacker = dmgMatches?.groupValues?.getOrNull(2)
  val probablyCombatTimer =
    (combatTimer - (System.currentTimeMillis() - lastUpdatedCombatTime) / 1000.0).coerceAtLeast(
      0.0
    )

  if (attacked != null && attacked.isNotEmpty()) {
    val playersContainsAttacked = minecraft.world!!.players.any {
//      println("player: ${it.name.string}, $attacked")
      attacked.endsWith(it.name.string)
    }

    val attackedIsPlayer = attacked.contains(guildReg) || playersContainsAttacked
//      ) &&
//      !attacked.contains(' ')
    //      attacked.contains(rankReg) ||

//    println("attack: $attackedIsPlayer, $attacked")

    // Attacked is player pvp combat
    if (attackedIsPlayer) {
      // COMBAT BONUS WHOAHHO
      if (probablyCombatTimer <= 0.0) {
        lastUpdatedBonusTime = System.currentTimeMillis()
        bonusTimer = combatBonusTime
      }

      lastUpdatedCombatTime = System.currentTimeMillis()
      combatTimer = max(combatPvPTime, probablyCombatTimer)

//      println("setting timer: probably $probablyCombatTimer, $combatTimer now")
    } else {
      // Attacked is monster pve combat

      // COMBAT BONUS WHOAHHO
      if (probablyCombatTimer <= 0.0) {
        lastUpdatedBonusTime = System.currentTimeMillis()
        bonusTimer = combatBonusTime
      }

      lastUpdatedCombatTime = System.currentTimeMillis()
      combatTimer = max(combatPvETime, probablyCombatTimer)
    }
  } else if (attacker != null && attacker.isNotEmpty()) {
    // MONSTER??
    if (attacker !in notRealCombat) {
      // COMBAT BONUS WHOAHHO
      if (probablyCombatTimer <= 0.0) {
        lastUpdatedBonusTime = System.currentTimeMillis()
        bonusTimer = combatBonusTime
      }

      val playersContainsAttacker = minecraft.world!!.players.any {
        it.name.string == attacker
      }

      lastUpdatedCombatTime = System.currentTimeMillis()
      combatTimer = if (playersContainsAttacker) {
        max(combatPvPTime, probablyCombatTimer)
      } else {
        max(combatPvETime, probablyCombatTimer)
      }
    }
  }
}