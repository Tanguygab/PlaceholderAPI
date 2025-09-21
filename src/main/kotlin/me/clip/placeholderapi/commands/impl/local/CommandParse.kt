package me.clip.placeholderapi.commands.impl.local

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.PlaceholderAPIPlugin
import me.clip.placeholderapi.commands.PlaceholderCommand
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.util.Msg
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandParse : PlaceholderCommand("parse", "bcparse", "parserel", "cmdparse") {
    override fun evaluate(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender,
        alias: String, params: List<String>
    ) {
        when (alias.lowercase()) {
            "parserel" -> evaluateParseRelation(sender, params)
            "parse" -> evaluateParseSingular(sender, params, false, false)
            "bcparse" -> evaluateParseSingular(sender, params, true, false)
            "cmdparse" -> evaluateParseSingular(sender, params, false, true)
        }
    }

    override fun complete(
        plugin: PlaceholderAPIPlugin,
        sender: CommandSender, alias: String,
        params: List<String>, suggestions: MutableList<String>
    ) {
        when (alias.lowercase()) {
            "parserel" -> completeParseRelation(params, suggestions)
            "parse", "bcparse", "cmdparse" -> completeParseSingular(sender, params, suggestions)
        }
    }


    private fun evaluateParseSingular(sender: CommandSender, params: List<String>, broadcast: Boolean, command: Boolean) {
        if (params.size < 2) {
            Msg.msg(
                sender,
                "&cYou must provide a target and message: &b/papi "
                        + (if (command) "cmdparse" else if (broadcast) "bcparse" else "parse")
                        + " &7{target} &a{message}"
            )
            return
        }

        val player: OfflinePlayer?

        if ("me".equals(params[0], ignoreCase = true)) {
            if (sender !is Player) {
                Msg.msg(sender, "&cYou must be a player to use &7me&c as a target!")
                return
            }

            player = sender
        } else if ("--null".equals(params[0], ignoreCase = true)) {
            player = null
        } else {
            val target = resolvePlayer(params[0])
            if (target == null) {
                Msg.msg(sender, "&cFailed to find player: &7" + params[0])
                return
            }

            player = target
        }

        val message = PlaceholderAPI.setPlaceholders(player, params.subList(1, params.size).joinToString(" "))

        if (command) Bukkit.dispatchCommand(sender, message)
        else if (broadcast) Bukkit.broadcastMessage(message)
        else sender.sendMessage(message)
    }

    private fun evaluateParseRelation(sender: CommandSender, params: List<String>) {
        if (params.size < 3) {
            Msg.msg(
                sender,
                "&cYou must supply two targets, and a message: &b/papi parserel &7{target one} "
                        + "{target two} &a{message}"
            )
            return
        }

        val playerOne: OfflinePlayer?

        if ("me".equals(params[0], ignoreCase = true)) {
            if (sender !is Player) {
                Msg.msg(sender, "&cYou must be a player to use &7me&c as a target!")
                return
            }

            playerOne = sender
        } else {
            playerOne = resolvePlayer(params[0])
        }

        if (playerOne == null || !playerOne.isOnline) {
            Msg.msg(sender, "&cFailed to find player: &f" + params[0])
            return
        }

        val playerTwo: OfflinePlayer?

        if ("me".equals(params[1], ignoreCase = true)) {
            if (sender !is Player) {
                Msg.msg(sender, "&cYou must be a player to use &7me&c as a target!")
                return
            }

            playerTwo = sender
        } else {
            playerTwo = resolvePlayer(params[1])
        }

        if (playerTwo == null || !playerTwo.isOnline) {
            Msg.msg(sender, "&cFailed to find player: &f" + params[1])
            return
        }

        val message = PlaceholderAPI.setRelationalPlaceholders(
            playerOne as Player, playerTwo as Player,
            params.subList(2, params.size).joinToString(" ")
        )

        sender.sendMessage(message)
    }


    private fun completeParseSingular(sender: CommandSender, params: List<String>, suggestions: MutableList<String>) {
        if (params.size <= 1) {
            if (sender is Player && (params.isEmpty() || "me"
                    .startsWith(params[0].lowercase()))
            ) {
                suggestions.add("me")
            }

            if ("--null".startsWith(params[0].lowercase())) {
                suggestions.add("--null")
            }

            val names = Bukkit.getOnlinePlayers().map { it.name }
            suggestByParameter(names, suggestions, if (params.isEmpty()) null else params[0])

            return
        }

        val name = params[params.size - 1]
        if (!name.startsWith("%") || name.endsWith("%")) {
            return
        }

        val index = name.indexOf('_')
        if (index == -1) {
            return  // no arguments supplied yet
        }

        val expansion: PlaceholderExpansion = PlaceholderAPIPlugin.Companion.getInstance().localExpansionManager
            .findExpansionByIdentifier(name.substring(1, index)) ?: return

        val possible = expansion.placeholders.toMutableSet()

        PlaceholderAPIPlugin.getInstance()
            .cloudExpansionManager
            .findCloudExpansionByName(expansion.name)
            ?.let { possible.addAll(it.placeholders!!) }

        suggestByParameter(possible, suggestions, params[params.size - 1])
    }

    private fun completeParseRelation(
        params: List<String>,
        suggestions: MutableList<String>
    ) {
        if (params.size > 2) return

        val names = Bukkit.getOnlinePlayers().map { it.name }
        suggestByParameter(names, suggestions, if (params.isEmpty()) null else params[params.size - 1])
    }


    private fun resolvePlayer(name: String): OfflinePlayer? {
        var target: OfflinePlayer? = Bukkit.getPlayerExact(name)

        if (target == null) {
            // Not the best option, but Spigot doesn't offer a good replacement (as usual)
            target = Bukkit.getOfflinePlayer(name)

            return if (target.hasPlayedBefore()) target else null
        }

        return target
    }
}