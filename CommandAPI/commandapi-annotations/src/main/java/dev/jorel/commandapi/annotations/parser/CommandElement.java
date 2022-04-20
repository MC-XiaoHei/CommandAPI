package dev.jorel.commandapi.annotations.parser;

import java.io.PrintWriter;
import java.util.Optional;
import java.util.Stack;

import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.annotations.Utils;

public abstract class CommandElement {

	/**
	 * Emits the current ADT.
	 * 
	 * @param out the print writer to write to
	 */
	public abstract void emit(PrintWriter out, int currentIndentation);

	public int indentation;

	public String indentation() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < indentation; i++) {
			builder.append("    ");
		}
		return builder.toString();
	};

	public void indent() {
		indentation++;
	}

	public void dedent() {
		indentation--;
		if (indentation < 0) {
			indentation = 0;
		}
	}

	public void emitPermission(PrintWriter out, CommandPermission permission) {
		if (permission.equals(CommandPermission.NONE)) {
			// Do nothing
		} else if (permission.equals(CommandPermission.OP)) {
			out.println();
			out.print(indentation() + ".withPermission(CommandPermission.OP)");
		} else {
			out.println();
			if (permission.isNegated()) {
				out.print(indentation() + ".withoutPermission(\"");
			} else {
				out.print(indentation() + ".withPermission(\"");
			}
			out.print(permission.getPermission());
			out.println("\")");
		}
	}

	public void emitSuggestion(PrintWriter out, Optional<SuggestionClass> suggestions, CommandData parent) {
		if (suggestions.isPresent()) {
			SuggestionClass suggestion = suggestions.get();

			if (suggestion.isSafeSuggestions()) {
				// TODO: Semantics must check that whatever we're applying these suggestions to
				// implements SafeOverrideableArgument.
				// TODO: Semantics must check that the type argument of
				// SafeOverrideableArgument<?> matches this.primitive
				out.print(indentation() + ".replaceSafeSuggestions(");
			} else {
				out.print(indentation() + ".replaceSuggestions(");
			}

			// If the suggestion implementation class is a top-level class, we can literally
			// just instantiate it
			if (suggestion.typeElement().getNestingKind() == NestingKind.TOP_LEVEL) {
				out.print("new " + suggestion.typeElement().getSimpleName() + "().get())");
			} else {
				// If it's not a top-level class, we have to assume it's declared in some class
				// within @Command or @Subcommand. TODO: This should be checked during
				// semantics!

				// We need to derive the path of classes required to get to this suggestion
				// class, from the top-level @Command class
				CommandData topLevelCommand = parent;
				while (topLevelCommand.getParent() != null) {
					topLevelCommand = topLevelCommand.getParent();
				}

				Stack<TypeElement> typeStack = new Stack<>();

				TypeElement currentTypeElement = suggestion.typeElement();
				Types types = suggestion.processingEnv().getTypeUtils();

				while (!types.isSameType(currentTypeElement.asType(), topLevelCommand.getTypeElement().asType())) {
					typeStack.add(currentTypeElement);

					if (currentTypeElement.getNestingKind() == NestingKind.TOP_LEVEL) {
						break;
					} else {
						// TODO: We've assumed it's a type element. It's possible that the enclosing
						// element is an executable element if we've declared this class inside a
						// function (very very improbable, but possible!)
						currentTypeElement = (TypeElement) currentTypeElement.getEnclosingElement();
					}
				}
				
				// TODO: We should probably store this variable name somewhere in a static final location
				out.print(Utils.COMMAND_VAR_NAME);
				for(TypeElement typeElement : typeStack) {
					out.print(".new " + typeElement.getSimpleName() + "()");
				}
				out.print(".get())");
			}
		}
	}

}