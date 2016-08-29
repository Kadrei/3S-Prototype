package de.thmgames.s3.Utils;

import android.content.Context;

import com.parse.ParseException;

import de.thmgames.s3.Activities.SignUpOrInActivity;
import de.thmgames.s3.R;

/**
 * Created by Benedikt on 11.12.2014.
 */
public final class ParseErrorUtils {
    public static final int ACCOUNT_ALREADY_LINKED = 208;
    public static final int CACHE_MISS = 120;
    public static final int COMMAND_UNAVAILABLE = 108;
    public static final int CONNECTION_FAILED = 100;
    public static final int DUPLICATE_VALUE = 137;
    public static final int EMAIL_MISSING = 204;
    public static final int EMAIL_NOT_FOUND = 205;
    public static final int EMAIL_TAKEN = 203;
    public static final int EXCEEDED_QUOTA = 140;
    public static final int FILE_DELETE_ERROR = 153;
    public static final int INCORRECT_TYPE = 111;
    public static final int INTERNAL_SERVER_ERROR = 1;
    public static final int INVALID_ACL = 123;
    public static final int INVALID_CHANNEL_NAME = 112;
    public static final int INVALID_CLASS_NAME = 103;
    public static final int INVALID_EMAIL_ADDRESS = 125;
    public static final int INVALID_EVENT_NAME = 160;
    public static final int INVALID_FILE_NAME = 122;
    public static final int INVALID_JSON = 107;
    public static final int INVALID_KEY_NAME = 105;
    public static final int INVALID_LINKED_SESSION = 251;
    public static final int INVALID_NESTED_KEY = 121;
    public static final int INVALID_POINTER = 106;
    public static final int INVALID_QUERY = 102;
    public static final int INVALID_ROLE_NAME = 139;
    public static final int LINKED_ID_MISSING = 250;
    public static final int MISSING_OBJECT_ID = 104;
    public static final int MUST_CREATE_USER_THROUGH_SIGNUP = 207;
    public static final int NOT_INITIALIZED = 109;
    public static final int OBJECT_NOT_FOUND = 101;
    public static final int OBJECT_TOO_LARGE = 116;
    public static final int OPERATION_FORBIDDEN = 119;
    public static final int OTHER_CAUSE = -1;
    public static final int PASSWORD_MISSING = 201;
    public static final int PUSH_MISCONFIGURED = 115;
    public static final int SCRIPT_ERROR = 141;
    public static final int SESSION_MISSING = 206;
    public static final int TIMEOUT = 124;
    public static final int UNSUPPORTED_SERVICE = 252;
    public static final int USERNAME_MISSING = 200;
    public static final int USERNAME_TAKEN = 202;
    public static final int VALIDATION_ERROR = 142;


    public static String getErrorMessageFor(Context ctx, ParseException e) {
        boolean isInSignUp = ctx instanceof SignUpOrInActivity;
        String errorMessage = ctx.getResources().getString(R.string.default_non_user_error);
        switch (e.getCode()) {
            case ACCOUNT_ALREADY_LINKED:
                errorMessage = ctx.getResources().getString(R.string.account_already_linked_208);
                break;
            case CACHE_MISS:
                errorMessage = ctx.getResources().getString(R.string.cache_miss_120);
                break;
            case COMMAND_UNAVAILABLE:
                errorMessage = ctx.getResources().getString(R.string.command_unavailable_108);
                break;
            case CONNECTION_FAILED:
                errorMessage = ctx.getResources().getString(R.string.connection_failed_100);
                break;
            case DUPLICATE_VALUE:
                errorMessage = ctx.getResources().getString(R.string.duplicate_value_137);
                break;
            case EMAIL_MISSING:
                errorMessage = ctx.getResources().getString(R.string.email_missing_204);
                break;
            case EMAIL_NOT_FOUND:
                errorMessage = ctx.getResources().getString(R.string.email_not_found_205);
                break;
            case EMAIL_TAKEN :
                errorMessage = ctx.getResources().getString(R.string.email_taken_203);
                break;
            case EXCEEDED_QUOTA:
                errorMessage = ctx.getResources().getString(R.string.exceeded_quota_140);
                break;
            case FILE_DELETE_ERROR:
                errorMessage = ctx.getResources().getString(R.string.file_delete_error_153);
                break;
            case INCORRECT_TYPE:
                errorMessage = ctx.getResources().getString(R.string.incorrect_type_111);
                break;
            case INTERNAL_SERVER_ERROR:
                errorMessage = ctx.getResources().getString(R.string.internal_server_error_1);
                break;
            case INVALID_ACL:
                errorMessage = ctx.getResources().getString(R.string.invalid_acl_123);
                break;
            case INVALID_CHANNEL_NAME:
                errorMessage = ctx.getResources().getString(R.string.invalid_channel_name_112);
                break;
            case INVALID_CLASS_NAME:
                errorMessage = ctx.getResources().getString(R.string.invalid_class_name_103);
                break;
            case INVALID_EMAIL_ADDRESS:
                errorMessage = ctx.getResources().getString(R.string.invalid_email_adress_125);
                break;
            case INVALID_EVENT_NAME:
                errorMessage = ctx.getResources().getString(R.string.invalid_event_name_160);
                break;
            case INVALID_FILE_NAME:
                errorMessage = ctx.getResources().getString(R.string.invalid_file_name_122);
                break;
            case INVALID_JSON:
                errorMessage = ctx.getResources().getString(R.string.invalid_json_107);
                break;
            case INVALID_KEY_NAME:
                errorMessage = ctx.getResources().getString(R.string.invalid_key_name_105);
                break;
            case INVALID_LINKED_SESSION:
                errorMessage = ctx.getResources().getString(R.string.invalid_linked_session_251);
                break;
            case INVALID_NESTED_KEY:
                errorMessage = ctx.getResources().getString(R.string.invalid_nested_key_121);
                break;
            case INVALID_POINTER:
                errorMessage = ctx.getResources().getString(R.string.invalid_pointer_106);
                break;
            case INVALID_QUERY:
                errorMessage = ctx.getResources().getString(R.string.invalid_query_102);
                break;
            case INVALID_ROLE_NAME:
                errorMessage = ctx.getResources().getString(R.string.invalid_role_name_139);
                break;
            case LINKED_ID_MISSING :
                errorMessage = ctx.getResources().getString(R.string.linked_id_missing_250);
                break;
            case MISSING_OBJECT_ID :
                errorMessage = ctx.getResources().getString(R.string.missing_object_id_104);
                break;
            case MUST_CREATE_USER_THROUGH_SIGNUP :
                errorMessage = ctx.getResources().getString(R.string.must_create_user_through_signup_207);
                break;
            case NOT_INITIALIZED :
                errorMessage = ctx.getResources().getString(R.string.not_initialized_109);
                break;
            case OBJECT_NOT_FOUND:
                errorMessage = isInSignUp ? ctx.getString(R.string.invalid_login_credentials) :ctx.getResources().getString(R.string.object_not_found_101);
                break;
            case OBJECT_TOO_LARGE :
                errorMessage = ctx.getResources().getString(R.string.object_too_large_116);
                break;
            case OPERATION_FORBIDDEN:
                errorMessage = ctx.getResources().getString(R.string.operation_forbidden_119);
                break;
            case OTHER_CAUSE:
                errorMessage = ctx.getResources().getString(R.string.other_cause_1);
                break;
            case PASSWORD_MISSING:
                errorMessage = ctx.getResources().getString(R.string.password_missing_201);
                break;
            case PUSH_MISCONFIGURED:
                errorMessage = ctx.getResources().getString(R.string.push_misconfigured_115);
                break;
            case SCRIPT_ERROR:
                errorMessage = ctx.getResources().getString(R.string.script_error_141);
                break;
            case SESSION_MISSING:
                errorMessage = ctx.getResources().getString(R.string.session_missing_206);
                break;
            case TIMEOUT:
                errorMessage = ctx.getResources().getString(R.string.timeout_124);
                break;
            case UNSUPPORTED_SERVICE:
                errorMessage = ctx.getResources().getString(R.string.unsupported_service_252);
                break;
            case USERNAME_MISSING:
                errorMessage = ctx.getResources().getString(R.string.username_missing_200);
                break;
            case USERNAME_TAKEN:
                errorMessage = ctx.getResources().getString(R.string.username_taken_202);
                break;
            case VALIDATION_ERROR:
                errorMessage = ctx.getResources().getString(R.string.validation_error_142);
                break;
        }
        return errorMessage;
    }
}
