package cafe.woden.ircclient.ui.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Swing client-property keys not exposed by the current UI libraries. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SwingClientProperties {

  public static final String PASSWORD_FIELD_SHOW_REVEAL_BUTTON = "JPasswordField.showRevealButton";
}
