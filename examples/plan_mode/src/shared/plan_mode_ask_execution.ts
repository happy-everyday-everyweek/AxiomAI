import { resolvePlanModeI18n } from "./plan_mode_i18n.js";
import { readSingleActiveChatView } from "./plan_mode_state.js";

export type SubmitPlanaskAnswersResult = {
  success: boolean;
  error?: string;
};

export function submitPlanaskAnswers(
  message: string
): SubmitPlanaskAnswersResult {
  const text = resolvePlanModeI18n();
  try {
    const activeView = readSingleActiveChatView();
    if (!activeView) {
      void Tools.System.toast(text.toastChatViewMissing);
      return {
        success: false,
        error: text.toastChatViewMissing,
      };
    }

    void Tools.Chat.sendMessage(
      message,
      activeView.chatId,
      undefined,
      undefined,
      { runtime: activeView.runtime }
    ).catch((error) => {
      const errorText = error instanceof Error
        ? error.message || "error"
        : (typeof error === "string" || error == null ? error || "error" : "error");
      const toastMessage = `${text.askToastAnswerSendFailedPrefix}${errorText}`;
      void Tools.System.toast(toastMessage);
    });
    void Tools.System.toast(text.askToastAnswerSent);
    return { success: true };
  } catch (error) {
    const errorText = error instanceof Error
      ? error.message || "error"
      : (typeof error === "string" || error == null ? error || "error" : "error");
    const message = `${text.askToastAnswerSendFailedPrefix}${errorText}`;
    void Tools.System.toast(message);
    return {
      success: false,
      error: message,
    };
  }
}
