package dev.jorel.commandapi.nms;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.SpigotCommandRegistration;
import dev.jorel.commandapi.preprocessor.Differs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_18_R1.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_18_R1.command.VanillaCommandWrapper;

public class PaperNMS_1_18_R1 extends PaperNMS_Common {

	private NMS_1_18_R1 bukkitNMS;

	@Override
	public Component getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(ComponentArgument.getComponent(cmdCtx, key)));
	}

	@Override
	public NamedTextColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		final Integer color = ColorArgument.getColor(cmdCtx, key).getColor();
		return color == null ? NamedTextColor.WHITE : NamedTextColor.ofExact(color);
	}

	@Override
	public NMS<?> bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_1_18_R1();
		}
		return bukkitNMS;
	}

	@Override
	@Differs(from = "1.17", by = "MinecraftServer#getCommands -> MinecraftServer#aA")
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return new SpigotCommandRegistration<>(
			bukkitNMS.<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher.getDispatcher(),
			(SimpleCommandMap) getCommandMap(),
			() -> bukkitNMS.<MinecraftServer>getMinecraftServer().getCommands().getDispatcher(),
			command -> command instanceof VanillaCommandWrapper,
			node -> new VanillaCommandWrapper(bukkitNMS.<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher, node),
			node -> node.getCommand() instanceof BukkitCommandWrapper
		);
	}

}
