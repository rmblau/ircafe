package cafe.woden.ircclient.irc.matrix;

import cafe.woden.ircclient.irc.ircv3.Ircv3ChatHistorySelectors;
import cafe.woden.ircclient.util.Ircv3CapabilityNames;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Matrix event, JSON, and IRCv3 bridge token constants. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MatrixProtocol {

  static final String ACCESS_TOKEN_BLANK = "access token is blank";
  static final String ENCRYPTED_PLACEHOLDER_BODY = "[encrypted message unavailable]";

  static final String EVENT_REACTION = "m.reaction";
  static final String EVENT_DIRECT = "m.direct";
  static final String EVENT_ROOM_ENCRYPTED = "m.room.encrypted";
  static final String EVENT_ROOM_MEMBER = "m.room.member";
  static final String EVENT_ROOM_MESSAGE = "m.room.message";
  static final String EVENT_ROOM_POWER_LEVELS = "m.room.power_levels";
  static final String EVENT_ROOM_REDACTION = "m.room.redaction";
  static final String EVENT_ROOM_TOPIC = "m.room.topic";

  static final String HISTORY_SELECTOR_MSGID_PREFIX = Ircv3ChatHistorySelectors.MSGID_PREFIX;
  static final String HISTORY_SELECTOR_TIMESTAMP_PREFIX =
      Ircv3ChatHistorySelectors.TIMESTAMP_PREFIX;

  static final String JSON_BODY = "body";
  static final String JSON_ACCOUNT_DATA = "account_data";
  static final String JSON_ALIASES = "aliases";
  static final String JSON_CHUNK = "chunk";
  static final String JSON_CONTENT = "content";
  static final String JSON_DISPLAY_NAME = "displayname";
  static final String JSON_EPHEMERAL = "ephemeral";
  static final String JSON_EVENT_ID = "event_id";
  static final String JSON_EVENTS = "events";
  static final String JSON_JOIN = "join";
  static final String JSON_JOINED_ROOMS = "joined_rooms";
  static final String JSON_KEY = "key";
  static final String JSON_MEMBERSHIP = "membership";
  static final String JSON_MSGTYPE = "msgtype";
  static final String JSON_NEW_CONTENT = "m.new_content";
  static final String JSON_NEXT_BATCH = "next_batch";
  static final String JSON_ORIGIN_SERVER_TS = "origin_server_ts";
  static final String JSON_PREV_CONTENT = "prev_content";
  static final String JSON_REASON = "reason";
  static final String JSON_RELATES_TO = "m.relates_to";
  static final String JSON_RELATION_TYPE = "rel_type";
  static final String JSON_REPLY_TO = "m.in_reply_to";
  static final String JSON_REPLY_TO_LEGACY = "in_reply_to";
  static final String JSON_ROOM_ID = "room_id";
  static final String JSON_ROOMS = "rooms";
  static final String JSON_SENDER = "sender";
  static final String JSON_STATE = "state";
  static final String JSON_STATE_KEY = "state_key";
  static final String JSON_TIMELINE = "timeline";
  static final String JSON_TYPE = "type";
  static final String JSON_UNSIGNED = "unsigned";
  static final String JSON_URL = "url";
  static final String JSON_USER_ID = "user_id";

  static final String MSGTYPE_AUDIO = "m.audio";
  static final String MSGTYPE_EMOTE = "m.emote";
  static final String MSGTYPE_FILE = "m.file";
  static final String MSGTYPE_IMAGE = "m.image";
  static final String MSGTYPE_NOTICE = "m.notice";
  static final String MSGTYPE_TEXT = "m.text";
  static final String MSGTYPE_VIDEO = "m.video";
  static final Set<String> MEDIA_MSGTYPES =
      Set.of(MSGTYPE_IMAGE, MSGTYPE_FILE, MSGTYPE_VIDEO, MSGTYPE_AUDIO);

  static final String READ_MARKER_FULLY_READ = "m.fully_read";
  static final String READ_MARKER_READ = "m.read";
  static final String READ_MARKER_READ_PRIVATE = "m.read.private";
  static final String RELATION_ANNOTATION = "m.annotation";
  static final String RELATION_REPLACE = "m.replace";
  static final String LOGIN_TYPE_PASSWORD = "m.login.password";
  static final String MEMBERSHIP_JOIN = "join";

  static final String TAG_DRAFT_EDIT = "draft/edit";
  static final String TAG_DRAFT_REACT = Ircv3CapabilityNames.DRAFT_REACT;
  static final String TAG_DRAFT_REPLY = Ircv3CapabilityNames.DRAFT_REPLY;
  static final String TAG_DRAFT_UNREACT = Ircv3CapabilityNames.DRAFT_UNREACT;
  static final String TAG_REPLY = Ircv3CapabilityNames.REPLY;
  static final String TAG_IRCAFE_PM_TARGET = "ircafe/pm-target";
  static final String TAG_MATRIX_MEDIA_URL = "matrix.media_url";
  static final String TAG_MATRIX_MSGTYPE = "matrix.msgtype";
  static final String TAG_MATRIX_ROOM_ID = "matrix.room_id";
  static final String TAG_MATRIX_UPLOAD_PATH = "matrix.upload_path";
  static final String TAG_MATRIX_URL = "matrix.url";
  static final String RAW_TAG_MATRIX_MEDIA_URL = "matrix/media_url";
  static final String RAW_TAG_MATRIX_MSGTYPE = "matrix/msgtype";
  static final String RAW_TAG_MATRIX_UPLOAD_PATH = "matrix/upload_path";
  static final String RAW_TAG_MATRIX_URL = "matrix/url";
}
