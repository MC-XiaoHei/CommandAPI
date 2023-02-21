package dev.jorel.commandapi.nms;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.ExceptionHandlingArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class ExceptionHandlingArgumentSerializer_1_17_Common<T> implements ArgumentSerializer<ExceptionHandlingArgumentType<T>> {
	private static final MethodHandle ArgumentTypes_getInfo;

	// Compute all var handles all in one go so we don't do this during main server runtime
	static {
		// We need a reference to the class object for ArgumentTypes.Entry, but that inner class is private
		// We can get an object from ArgumentTypes#get(ResourceLocation), then take its class
		Class<?> entryClass = null;
		try {
			Method getInfoByResourceLocation = ArgumentTypes.class.getDeclaredMethod("a", ResourceLocation.class);
			getInfoByResourceLocation.setAccessible(true);
			Object entryObject = getInfoByResourceLocation.invoke(null, new ResourceLocation("entity"));
			entryClass = entryObject.getClass();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}

		MethodHandle ar_b = null;
		try {
			ar_b = MethodHandles.privateLookupIn(ArgumentTypes.class, MethodHandles.lookup())
				.findStatic(ArgumentTypes.class, "b", MethodType.methodType(entryClass, ArgumentType.class));
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		ArgumentTypes_getInfo = ar_b;
	}

	@Override
	public void serializeToNetwork(ExceptionHandlingArgumentType<T> argument, FriendlyByteBuf friendlyByteBuf) {
		try {
			// Remove this key from packet
			Object myInfo = ArgumentTypes_getInfo.invoke(argument);

			// TODO: This Field reflection (and others in this class) acts on the class ArgumentTypes.Entry. This inner
			//  class is private, and the @RequireField annotation doesn't currently support that. We would like
			//  to check this reflection at compile-time though, but the preprocess needs to be expanded first
			Field keyField = CommandAPIHandler.getField(myInfo.getClass(), "c");
			String myKey = keyField.get(myInfo).toString();
			byte[] myKeyBytes = myKey.getBytes(StandardCharsets.UTF_8);
			// Removing length and size of string, assuming length is always written as 1 byte
			friendlyByteBuf.writerIndex(friendlyByteBuf.writerIndex() - myKeyBytes.length - 1);

			// Add baseType key instead
			ArgumentType<T> baseType = argument.baseType();
			Object baseInfo = ArgumentTypes_getInfo.invoke(baseType);
			String baseKey = keyField.get(baseInfo).toString();
			friendlyByteBuf.writeUtf(baseKey);

			// Serialize baseType
			Field subSerializerField = CommandAPIHandler.getField(baseInfo.getClass(), "b");
			ArgumentSerializer<ArgumentType<T>> subSerializer = (ArgumentSerializer<ArgumentType<T>>) subSerializerField.get(baseInfo);
			subSerializer.serializeToNetwork(baseType, friendlyByteBuf);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serializeToJson(ExceptionHandlingArgumentType<T> argument, JsonObject properties) {
		try {
			ArgumentType<T> baseType = argument.baseType();

			Object baseInfo = ArgumentTypes_getInfo.invoke(baseType);

			Field keyField = CommandAPIHandler.getField(baseInfo.getClass(), "c");
			properties.addProperty("baseType", keyField.get(baseInfo).toString());

			Field subSerializerField = CommandAPIHandler.getField(baseInfo.getClass(), "b");
			ArgumentSerializer<ArgumentType<T>> subSerializer = (ArgumentSerializer<ArgumentType<T>>) subSerializerField.get(baseInfo);

			JsonObject subProperties = new JsonObject();
			subSerializer.serializeToJson(baseType, subProperties);
			if (subProperties.size() > 0) {
				properties.add("baseProperties", subProperties);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public ExceptionHandlingArgumentType<T> deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		// Since this class overrides its ArgumentRegistry key with the baseType's,
		// this class's key should never show up in a packet and this method should never
		// be called to deserialize the ArgumentType info that wasn't put into the packet
		// anyway. Also, the server shouldn't ever deserialize a PacketPlay*Out*Commands
		// either. If this method ever gets called, either you or I are doing something very wrong!
		throw new IllegalStateException("This shouldn't happen! See dev.jorel.commandapi.nms.ExceptionHandlingArgumentSerializer_1_17_Common#deserializeFromNetwork for more information");
		// Including a mini-stacktrace here in case this exception shows up
		// on a client-disconnected screen, which is not very helpful
	}
}