package cafe.woden.ircclient.ui.settings.theme;

import cafe.woden.ircclient.ui.util.UiColorKeys;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@InterfaceLayer
@Lazy
class ThemePresetRegistry {

  record ThemePreset(String id, boolean dark, Map<String, String> extraDefaults) {}

  private static final Map<String, String> ORANGE_DARK_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#2F241E"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#F1DEC9"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#3A2C24"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#4A3328"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#281F1A"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#E48A33"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#D8751D"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#F0A14F"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#F0A14F"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#FFB367"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#A65414"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#FFF4E8"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#A65414"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#A65414"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#A65414"));

  private static final Map<String, String> BLUE_DARK_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#1E2734"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#DCEBFF"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#273447"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#2C3E56"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#18212C"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#4F8AD9"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#3B78C9"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#6DA2EA"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#6DA2EA"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#8BC0FF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#2F5F9E"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#F3F8FF"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#2F5F9E"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#2F5F9E"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#2F5F9E"));

  private static final Map<String, String> BLUE_LIGHT_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#EEF5FF"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#1F3552"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#FAFCFF"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#DCEBFF"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#E2EEFF"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#2E6FBE"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#2E6FBE"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#4C88D0"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#4C88D0"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#1D5DAA"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#B8D6FF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#10253F"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#B8D6FF"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#B8D6FF"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#B8D6FF"));

  private static final Map<String, String> NORDIC_LIGHT_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#ECEFF4"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#2E3440"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#F8FAFD"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#E5E9F0"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#E2E8F1"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#5E81AC"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#5E81AC"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#81A1C1"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#81A1C1"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#4C78A8"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#C9DAEE"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#1E2633"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#C9DAEE"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#C9DAEE"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#C9DAEE"));

  private static final Map<String, String> SOLARIZED_DARK_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#002B36"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#93A1A1"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#073642"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#0B3A46"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#00232C"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#268BD2"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#268BD2"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#2AA198"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#2AA198"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#268BD2"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#0A4A5C"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#EEE8D5"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#0A4A5C"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#0A4A5C"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#0A4A5C"));

  private static final Map<String, String> SOLARIZED_LIGHT_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#FDF6E3"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#586E75"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#FFFBF0"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#EEE8D5"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#F5EFD9"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#268BD2"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#268BD2"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#2AA198"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#2AA198"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#1E6FB0"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#D9ECFF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#073642"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#D9ECFF"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#D9ECFF"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#D9ECFF"));

  private static final Map<String, String> FOREST_DARK_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#1E2A22"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#D6E8DC"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#26362C"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#2D3F33"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#19241D"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#4FA36C"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#4FA36C"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#6FBD89"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#6FBD89"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#7CCB97"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#2F6A44"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#F3FAF6"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#2F6A44"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#2F6A44"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#2F6A44"));

  private static final Map<String, String> MINT_LIGHT_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#F2FBF7"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#20443A"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#FCFFFD"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#DDF3EA"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#E6F7F0"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#2E8F76"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#2E8F76"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#42A48A"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#42A48A"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#1F7D66"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#BDE8D9"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#10352C"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#BDE8D9"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#BDE8D9"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#BDE8D9"));

  private static final Map<String, String> RUBY_NIGHT_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#241E22"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#EBD8DF"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#2E252A"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#3A2C33"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#1E181B"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#C74B67"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#C74B67"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#D96883"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#D96883"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#E07E97"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#7C2F44"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#FFF1F5"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#7C2F44"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#7C2F44"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#7C2F44"));

  private static final Map<String, String> ARCTIC_LIGHT_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#F7FAFF"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#2A3A4E"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#FCFEFF"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#E8F0FF"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#EDF3FF"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#4B7BD8"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#4B7BD8"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#6A97EC"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#6A97EC"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#356BCF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#CCE0FF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#142843"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#CCE0FF"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#CCE0FF"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#CCE0FF"));

  private static final Map<String, String> GRAPHITE_MONO_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#252525"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#E4E4E4"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#2E2E2E"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#373737"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#1F1F1F"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#9FA3A8"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#9FA3A8"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#B6BABF"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#B6BABF"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#C2C7CC"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#525252"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#F8F8F8"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#525252"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#525252"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#525252"));

  private static final Map<String, String> TEAL_DEEP_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#1A2A2C"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#D5E7E6"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#213437"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#284045"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#152124"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#2FA7A0"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#2FA7A0"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#4DBCB5"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#4DBCB5"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#5CC9C3"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#216E6A"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#F1FCFB"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#216E6A"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#216E6A"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#216E6A"));

  private static final Map<String, String> SUNSET_DARK_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#2A2124"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#F2DFD8"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#34292D"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#402F35"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#231B1E"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#E28743"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#E28743"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#C76A56"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#C76A56"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#F2A367"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#7B3B45"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#FFF3EE"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#7B3B45"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#7B3B45"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#7B3B45"));

  private static final Map<String, String> TERMINAL_AMBER_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#151515"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#F2C98A"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#1D1D1D"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#262626"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#101010"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#E0A84A"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#E0A84A"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#F2BF68"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#F2BF68"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#FFC978"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#6F5121"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#FFF6E6"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#6F5121"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#6F5121"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#6F5121"));

  private static final Map<String, String> HIGH_CONTRAST_DARK_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#101214"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#F5F7FA"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#171B1F"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#1E242A"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#0C0F12"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#5CA9FF"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#5CA9FF"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#85C1FF"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#85C1FF"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#8CC5FF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#254A72"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#FFFFFF"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#254A72"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#254A72"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#254A72"));

  private static final Map<String, String> CRT_GREEN_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#0B100B"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#9BF2A6"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#101710"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#152015"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#090E09"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#57D36E"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#42BF5C"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#7EEA92"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#7EEA92"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#8EF7A3"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#1F5C2A"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#E8FFE9"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#1F5C2A"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#1F5C2A"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#1F5C2A"));

  private static final Map<String, String> CDE_BLUE_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#D5DCE9"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#13243A"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#E2E8F2"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#C9D3E2"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#C5CFDF"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#2D63A8"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#2D63A8"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#4F80BE"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#4F80BE"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#285A99"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#AFC3E5"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#0D1D33"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#AFC3E5"),
          Map.entry(UiColorKeys.LIST_SELECTION_FOREGROUND, "#0D1D33"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#AFC3E5"),
          Map.entry(UiColorKeys.TABLE_SELECTION_FOREGROUND, "#0D1D33"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#AFC3E5"),
          Map.entry(UiColorKeys.TREE_SELECTION_FOREGROUND, "#0D1D33"));

  private static final Map<String, String> TOKYO_NIGHT_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#1A1B26"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#C0CAF5"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#202331"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#2A2F45"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#171925"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#7AA2F7"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#7AA2F7"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#9AB8FF"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#9AB8FF"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#A9C2FF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#3A4B7A"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#F3F6FF"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#3A4B7A"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#3A4B7A"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#3A4B7A"));

  private static final Map<String, String> CATPPUCCIN_MOCHA_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#1E1E2E"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#CDD6F4"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#24273A"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#313244"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#181825"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#89B4FA"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#89B4FA"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#B4BEFE"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#B4BEFE"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#A6C8FF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#45475A"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#F5F7FF"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#45475A"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#45475A"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#45475A"));

  private static final Map<String, String> GRUVBOX_DARK_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#282828"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#EBDBB2"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#32302F"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#3C3836"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#1D2021"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#D79921"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#D79921"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#FABD2F"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#FABD2F"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#FFD266"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#665C54"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#FBF1C7"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#665C54"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#665C54"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#665C54"));

  private static final Map<String, String> GITHUB_SOFT_LIGHT_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#FFFFFF"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#24292F"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#F6F8FA"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#EFF2F5"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#F3F5F7"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#0969DA"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#0969DA"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#218BFF"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#218BFF"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#0550AE"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#DDF4FF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#0A3069"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#DDF4FF"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#DDF4FF"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#DDF4FF"));

  private static final Map<String, String> VIOLET_NEBULA_DEFAULTS =
      Map.ofEntries(
          Map.entry(UiColorKeys.FLAT_BACKGROUND, "#1B1629"),
          Map.entry(UiColorKeys.FLAT_FOREGROUND, "#E9E3FF"),
          Map.entry(UiColorKeys.FLAT_COMPONENT_BACKGROUND, "#241D37"),
          Map.entry(UiColorKeys.FLAT_BUTTON_BACKGROUND, "#2D2343"),
          Map.entry(UiColorKeys.FLAT_MENU_BACKGROUND, "#161126"),
          Map.entry(UiColorKeys.ACCENT_COLOR, "#8A63F5"),
          Map.entry(UiColorKeys.ACCENT_BASE_COLOR, "#7A54EC"),
          Map.entry(UiColorKeys.ACCENT_BASE_2_COLOR, "#A07CFF"),
          Map.entry(UiColorKeys.COMPONENT_FOCUS_COLOR, "#B292FF"),
          Map.entry(UiColorKeys.COMPONENT_LINK_COLOR, "#C2A7FF"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_BACKGROUND, "#4A3688"),
          Map.entry(UiColorKeys.TEXT_COMPONENT_SELECTION_FOREGROUND, "#F7F3FF"),
          Map.entry(UiColorKeys.LIST_SELECTION_BACKGROUND, "#4A3688"),
          Map.entry(UiColorKeys.TABLE_SELECTION_BACKGROUND, "#4A3688"),
          Map.entry(UiColorKeys.TREE_SELECTION_BACKGROUND, "#4A3688"));

  private static final Map<String, ThemePreset> PRESETS_BY_ID = buildPresetsById();

  ThemePreset byId(String id) {
    if (id == null || id.isBlank()) return null;
    return PRESETS_BY_ID.get(id.toLowerCase(java.util.Locale.ROOT));
  }

  private static Map<String, ThemePreset> buildPresetsById() {
    Map<String, ThemePreset> map = new LinkedHashMap<>();

    add(map, "crt-green", true, CRT_GREEN_DEFAULTS);
    add(map, "cde-blue", false, CDE_BLUE_DEFAULTS);

    add(map, "tokyo-night", true, TOKYO_NIGHT_DEFAULTS);
    add(map, "catppuccin-mocha", true, CATPPUCCIN_MOCHA_DEFAULTS);
    add(map, "gruvbox-dark", true, GRUVBOX_DARK_DEFAULTS);
    add(map, "github-soft-light", false, GITHUB_SOFT_LIGHT_DEFAULTS);

    add(map, "blue-dark", true, BLUE_DARK_DEFAULTS);
    add(map, "violet-nebula", true, VIOLET_NEBULA_DEFAULTS);
    add(map, "high-contrast-dark", true, HIGH_CONTRAST_DARK_DEFAULTS);
    add(map, "graphite-mono", true, GRAPHITE_MONO_DEFAULTS);
    add(map, "forest-dark", true, FOREST_DARK_DEFAULTS);
    add(map, "ruby-night", true, RUBY_NIGHT_DEFAULTS);
    add(map, "solarized-dark", true, SOLARIZED_DARK_DEFAULTS);
    add(map, "sunset-dark", true, SUNSET_DARK_DEFAULTS);
    add(map, "terminal-amber", true, TERMINAL_AMBER_DEFAULTS);
    add(map, "teal-deep", true, TEAL_DEEP_DEFAULTS);
    add(map, "orange", true, ORANGE_DARK_DEFAULTS);

    add(map, "nordic-light", false, NORDIC_LIGHT_DEFAULTS);
    add(map, "blue-light", false, BLUE_LIGHT_DEFAULTS);
    add(map, "arctic-light", false, ARCTIC_LIGHT_DEFAULTS);
    add(map, "mint-light", false, MINT_LIGHT_DEFAULTS);
    add(map, "solarized-light", false, SOLARIZED_LIGHT_DEFAULTS);

    return Map.copyOf(map);
  }

  private static void add(
      Map<String, ThemePreset> map, String id, boolean dark, Map<String, String> defaults) {
    map.put(id, new ThemePreset(id, dark, defaults));
  }
}
