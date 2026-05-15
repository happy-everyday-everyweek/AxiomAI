import * as planModeMode from "./plan_mode_mode.js";
import * as planModeI18n from "./plan_mode_i18n.js";
import * as planModePlanFile from "./plan_mode_plan_file.js";
import * as planModeState from "./plan_mode_state.js";

export type StartPlanImplementationResult = {
  success: boolean;
  error?: string;
};

export async function startPlanImplementation(
  planContent: string
): Promise<StartPlanImplementationResult> {
  const text = planModeI18n.resolvePlanModeI18n();
  const normalizedPlanContent = planContent.trim();
  if (!normalizedPlanContent) {
    const message = text.toastPlanEmpty;
    await Tools.System.toast(message);
    return { success: false, error: message };
  }

  try {
    const activeView = planModeState.readSingleActiveChatView();
    if (!activeView) {
      await Tools.System.toast(text.toastChatViewMissing);
      return { success: false, error: text.toastChatViewMissing };
    }
    const written = await planModePlanFile.writePlanFile(activeView.chatId, normalizedPlanContent);
    await planModeMode.disablePlanMode(written.chatId);
    void Tools.Chat.sendMessage(
      text.implementationMessage,
      written.chatId,
      undefined,
      undefined,
      { runtime: activeView.runtime }
    ).catch((error) => {
      const errorText = error instanceof Error
        ? error.message || "error"
        : (typeof error === "string" || error == null ? error || "error" : "error");
      const message = `${text.toastPlanSendFailedPrefix}${errorText}`;
      void Tools.System.toast(message);
    });
    void Tools.System.toast(text.toastPlanStarted);
    return { success: true };
  } catch (error) {
    const errorText = error instanceof Error
      ? error.message || "error"
      : (typeof error === "string" || error == null ? error || "error" : "error");
    const message = `${text.toastPlanWriteFailedPrefix}${errorText}`;
    await Tools.System.toast(message);
    return { success: false, error: message };
  }
}
