package dev.jorel.commandapi.kotlindsl

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.*
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.ConsoleCommandExecutor
import dev.jorel.commandapi.executors.ConsoleResultingCommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.jorel.commandapi.executors.PlayerResultingCommandExecutor
import dev.jorel.commandapi.executors.ResultingCommandExecutor
import java.util.function.Predicate

inline fun commandAPICommand(name: String, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).apply(command).register()
inline fun commandAPICommand(name: String, predicate: Predicate<CommandSource>, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).withRequirement(predicate).apply(command).register()

inline fun CommandAPICommand.argument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(base.apply(block))
fun CommandAPICommand.arguments(vararg arguments: Argument<*>): CommandAPICommand = withArguments(*arguments)

inline fun CommandAPICommand.optionalArgument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withOptionalArguments(base.apply(block))
fun CommandAPICommand.optionalArguments(vararg arguments: Argument<*>): CommandAPICommand = withOptionalArguments(*arguments)

inline fun subcommand(name: String, command: CommandAPICommand.() -> Unit = {}): CommandAPICommand = CommandAPICommand(name).apply(command)
fun CommandAPICommand.subcommand(command: CommandAPICommand): CommandAPICommand = withSubcommand(command)
inline fun CommandAPICommand.subcommand(name: String, command: CommandAPICommand.() -> Unit = {}): CommandAPICommand = withSubcommand(CommandAPICommand(name).apply(command))

// Integer arguments
inline fun CommandAPICommand.integerArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(IntegerArgument(nodeName).apply(block))
inline fun CommandAPICommand.integerArgument(nodeName: String, min: Int, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(IntegerArgument(nodeName, min).apply(block))
inline fun CommandAPICommand.integerArgument(nodeName: String, min: Int, max: Int, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(IntegerArgument(nodeName, min, max).apply(block))

// Float arguments
inline fun CommandAPICommand.floatArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(FloatArgument(nodeName).apply(block))
inline fun CommandAPICommand.floatArgument(nodeName: String, min: Float, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(FloatArgument(nodeName, min).apply(block))
inline fun CommandAPICommand.floatArgument(nodeName: String, min: Float, max: Float, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(FloatArgument(nodeName, min, max).apply(block))

// Double arguments
inline fun CommandAPICommand.doubleArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(DoubleArgument(nodeName).apply(block))
inline fun CommandAPICommand.doubleArgument(nodeName: String, min: Double, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(DoubleArgument(nodeName, min).apply(block))
inline fun CommandAPICommand.doubleArgument(nodeName: String, min: Double, max: Double, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(DoubleArgument(nodeName, min, max).apply(block))

// Long arguments
inline fun CommandAPICommand.longArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LongArgument(nodeName).apply(block))
inline fun CommandAPICommand.longArgument(nodeName: String, min: Long, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LongArgument(nodeName, min).apply(block))
inline fun CommandAPICommand.longArgument(nodeName: String, min: Long, max: Long, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LongArgument(nodeName, min, max).apply(block))

// Boolean argument
inline fun CommandAPICommand.booleanArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(BooleanArgument(nodeName).apply(block))

// String arguments
inline fun CommandAPICommand.stringArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(StringArgument(nodeName).apply(block))
inline fun CommandAPICommand.textArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(TextArgument(nodeName).apply(block))
inline fun CommandAPICommand.greedyStringArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(GreedyStringArgument(nodeName).apply(block))

// Literal arguments
inline fun CommandAPICommand.literalArgument(literal: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LiteralArgument.of(literal).apply(block))
inline fun CommandAPICommand.multiLiteralArgument(vararg literals: String, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(MultiLiteralArgument(*literals).apply(block))

// Requirements
inline fun CommandAPICommand.requirement(base: Argument<*>, predicate: Predicate<CommandSource>, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(base.withRequirement(predicate).apply(block))

// Command execution
fun CommandAPICommand.anyExecutor(any: (CommandSource, CommandArguments) -> Unit) = CommandAPICommandExecution().any(any).executes(this)
fun CommandAPICommand.playerExecutor(player: (Player, CommandArguments) -> Unit) = CommandAPICommandExecution().player(player).executes(this)
fun CommandAPICommand.consoleExecutor(console: (ConsoleCommandSource, CommandArguments) -> Unit) = CommandAPICommandExecution().console(console).executes(this)

fun CommandAPICommand.anyResultingExecutor(any: (CommandSource, CommandArguments) -> Int) = CommandAPICommandResultingExecution().any(any).executes(this)
fun CommandAPICommand.playerResultingExecutor(player: (Player, CommandArguments) -> Int) = CommandAPICommandResultingExecution().player(player).executes(this)
fun CommandAPICommand.consoleResultingExecutor(console: (ConsoleCommandSource, CommandArguments) -> Int) = CommandAPICommandResultingExecution().console(console).executes(this)

class CommandAPICommandExecution {

	private var any: ((CommandSource, CommandArguments) -> Unit)? = null
	private var player: ((Player, CommandArguments) -> Unit)? = null
	private var console: ((ConsoleCommandSource, CommandArguments) -> Unit)? = null

	fun any(any: (CommandSource, CommandArguments) -> Unit): CommandAPICommandExecution {
		this.any = any
		return this
	}

	fun player(player: (Player, CommandArguments) -> Unit): CommandAPICommandExecution {
		this.player = player
		return this
	}

	fun console(console: (ConsoleCommandSource, CommandArguments) -> Unit): CommandAPICommandExecution {
		this.console = console
		return this
	}

	fun executes(command: CommandAPICommand) {
		if (any != null) {
			command.executes(CommandExecutor { sender, args ->
				any?.invoke(sender, args)
			})
			return
		}
		if (player != null) {
			command.executesPlayer(PlayerCommandExecutor { player, args ->
				this.player?.invoke(player, args)
			})
			return
		}
		if (console != null) {
			command.executesConsole(ConsoleCommandExecutor { console, args ->
				this.console?.invoke(console, args)
			})
			return
		}
	}
}

class CommandAPICommandResultingExecution {

	private var any: ((CommandSource, CommandArguments) -> Int)? = null
	private var player: ((Player, CommandArguments) -> Int)? = null
	private var console: ((ConsoleCommandSource, CommandArguments) -> Int)? = null

	fun any(any: (CommandSource, CommandArguments) -> Int): CommandAPICommandResultingExecution {
		this.any = any
		return this
	}

	fun player(player: (Player, CommandArguments) -> Int): CommandAPICommandResultingExecution {
		this.player = player
		return this
	}

	fun console(console: (ConsoleCommandSource, CommandArguments) -> Int): CommandAPICommandResultingExecution {
		this.console = console
		return this
	}

	fun executes(command: CommandAPICommand) {
		if (any != null) {
			command.executes(ResultingCommandExecutor { sender, args ->
				any!!.invoke(sender, args)
			})
			return
		}
		if (player != null) {
			command.executesPlayer(PlayerResultingCommandExecutor { player, args ->
				this.player!!.invoke(player, args)
			})
			return
		}
		if (console != null) {
			command.executesConsole(ConsoleResultingCommandExecutor { console, args ->
				this.console!!.invoke(console, args)
			})
			return
		}
	}
}