package dev.jorel.commandapi.nms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;

import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.PreviewInfo;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.wrappers.PreviewableFunction;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;

public class NMS_1_19_R1_ChatPreviewHandler extends ChannelDuplexHandler {

	private final NMS<CommandSourceStack> nms;
	private final Plugin plugin;
	private final Player player;
	private final Connection connection;

	public NMS_1_19_R1_ChatPreviewHandler(NMS<CommandSourceStack> nms, Plugin plugin, Player player) {
		this.nms = nms;
		this.plugin = plugin;
		this.player = player;
		this.connection = ((CraftPlayer) player).getHandle().connection.connection;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ServerboundChatPreviewPacket chatPreview) {
			// Substring 1 because we want to get rid of the leading /
			final String fullInput = chatPreview.query().substring(1);
			ParseResults<CommandSourceStack> results = nms.getBrigadierDispatcher().parse(fullInput, nms.getCLWFromCommandSender(this.player));

			// Generate the path for lookup
			List<String> path = new ArrayList<>();
			for (ParsedCommandNode<CommandSourceStack> commandNode : results.getContext().getNodes()) {
				path.add(commandNode.getNode().getName());
			}
			PreviewableFunction<?> preview = CommandAPIHandler.getInstance().lookupPreviewable(path);

			// Calculate the (argument) input and generate the component to send
			String input = results.getContext().getNodes().get(results.getContext().getNodes().size() - 1).getRange().get(fullInput);

			final String jsonToSend;
			
			Object component = null;
			try {
				component = preview.generatePreview(new PreviewInfo(this.player, input, chatPreview.query()));
			} catch (WrapperCommandSyntaxException e) {
				component = TextComponent.fromLegacyText(e.getMessage() == null ? "" : e.getMessage());
			}
			
			if(component != null) {
				if(component instanceof BaseComponent[] baseComponent) {
					jsonToSend = ComponentSerializer.toString(baseComponent);
				} else if(CommandAPIHandler.getInstance().getPaper().isPresent()) {
					if(component instanceof Component adventureComponent) {
						jsonToSend = GsonComponentSerializer.gson().serialize(adventureComponent);
					} else {
						throw new IllegalArgumentException("Unexpected type returned from chat preview, got: " + component.getClass().getSimpleName());
					}
				} else {
					throw new IllegalArgumentException("Unexpected type returned from chat preview, got: " + component.getClass().getSimpleName());
				}
			} else {
				throw new NullPointerException("Returned value from chat preview was null");
			}

			if (jsonToSend != null) {
				Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.connection
					.send(new ClientboundChatPreviewPacket(chatPreview.queryId(), Serializer.fromJson(jsonToSend))));
			}
		}

		// Normal packet handling
		super.channelRead(ctx, msg);
	}

}
