"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.submitPlanaskAnswers = submitPlanaskAnswers;
const plan_mode_i18n_js_1 = require("./plan_mode_i18n.js");
const plan_mode_state_js_1 = require("./plan_mode_state.js");
function submitPlanaskAnswers(message) {
    const text = (0, plan_mode_i18n_js_1.resolvePlanModeI18n)();
    try {
        const activeView = (0, plan_mode_state_js_1.readSingleActiveChatView)();
        if (!activeView) {
            void Tools.System.toast(text.toastChatViewMissing);
            return {
                success: false,
                error: text.toastChatViewMissing,
            };
        }
        void Tools.Chat.sendMessage(message, activeView.chatId, undefined, undefined, { runtime: activeView.runtime }).catch((error) => {
            const errorText = error instanceof Error
                ? error.message || "error"
                : (typeof error === "string" || error == null ? error || "error" : "error");
            const toastMessage = `${text.askToastAnswerSendFailedPrefix}${errorText}`;
            void Tools.System.toast(toastMessage);
        });
        void Tools.System.toast(text.askToastAnswerSent);
        return { success: true };
    }
    catch (error) {
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
