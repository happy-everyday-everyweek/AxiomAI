package com.ai.assistance.operit.core.tools.javascript

import org.json.JSONObject

internal const val TOOLPKG_EXECUTION_ENTRY_FUNCTION = "__operitExecuteScriptFunction"

private fun buildExecutionPreludeSource(): String {
    return """
        function __operitGetActiveCallRuntime() {
            var root = typeof globalThis !== 'undefined'
                ? globalThis
                : (typeof window !== 'undefined' ? window : this);
            var runtime =
                root &&
                root.__operit_call_runtime_ref &&
                typeof root.__operit_call_runtime_ref === 'object'
                    ? root.__operit_call_runtime_ref
                    : __operit_call_runtime;
            return runtime && typeof runtime === 'object' ? runtime : __operit_call_runtime;
        }
        function __operitInvokeCallRuntime(methodName, argsLike) {
            var runtime = __operitGetActiveCallRuntime();
            var method = runtime ? runtime[methodName] : undefined;
            if (typeof method !== 'function') {
                return undefined;
            }
            return method.apply(runtime, Array.prototype.slice.call(argsLike || []));
        }
        function __operitInvokeCallRuntimeConsole(methodName, argsLike) {
            var runtime = __operitGetActiveCallRuntime();
            var runtimeConsole = runtime && runtime.console ? runtime.console : null;
            var method = runtimeConsole ? runtimeConsole[methodName] : undefined;
            if (typeof method !== 'function') {
                return undefined;
            }
            return method.apply(runtimeConsole, Array.prototype.slice.call(argsLike || []));
        }
        var sendIntermediateResult = function() { return __operitInvokeCallRuntime('sendIntermediateResult', arguments); };
        var emit = function() { return __operitInvokeCallRuntime('emit', arguments); };
        var delta = function() { return __operitInvokeCallRuntime('delta', arguments); };
        var log = function() { return __operitInvokeCallRuntime('log', arguments); };
        var update = function() { return __operitInvokeCallRuntime('update', arguments); };
        var done = function() { return __operitInvokeCallRuntime('done', arguments); };
        var complete = function() { return __operitInvokeCallRuntime('complete', arguments); };
        var getEnv = function() { return __operitInvokeCallRuntime('getEnv', arguments); };
        var getPluginConfigDir = function() { return __operitInvokeCallRuntime('getPluginConfigDir', arguments); };
        var getState = function() { return __operitInvokeCallRuntime('getState', arguments); };
        var getLang = function() { return __operitInvokeCallRuntime('getLang', arguments); };
        var getCallerName = function() { return __operitInvokeCallRuntime('getCallerName', arguments); };
        var getChatId = function() { return __operitInvokeCallRuntime('getChatId', arguments); };
        var getCallerCardId = function() { return __operitInvokeCallRuntime('getCallerCardId', arguments); };
        var __handleAsync = function() { return __operitInvokeCallRuntime('handleAsync', arguments); };
        var console = {
            log: function() { return __operitInvokeCallRuntimeConsole('log', arguments); },
            info: function() { return __operitInvokeCallRuntimeConsole('info', arguments); },
            warn: function() { return __operitInvokeCallRuntimeConsole('warn', arguments); },
            error: function() { return __operitInvokeCallRuntimeConsole('error', arguments); }
        };
        var reportDetailedError = function() { return __operitInvokeCallRuntime('reportDetailedError', arguments); };
        var ToolPkg = globalThis.ToolPkg;
        var Tools = globalThis.Tools;
        var Java = globalThis.Java;
        var Android = globalThis.Android;
        var Intent = globalThis.Intent;
        var PackageManager = globalThis.PackageManager;
        var ContentProvider = globalThis.ContentProvider;
        var SystemManager = globalThis.SystemManager;
        var DeviceController = globalThis.DeviceController;
        var OperitComposeDslRuntime = globalThis.OperitComposeDslRuntime;
        var CryptoJS = globalThis.CryptoJS;
        var Jimp = globalThis.Jimp;
        var UINode = globalThis.UINode;
        var OkHttpClientBuilder = globalThis.OkHttpClientBuilder;
        var OkHttpClient = globalThis.OkHttpClient;
        var RequestBuilder = globalThis.RequestBuilder;
        var OkHttp = globalThis.OkHttp;
        var pako = globalThis.pako;
        var _ = globalThis._;
        var dataUtils = globalThis.dataUtils;
        var toolCall = globalThis.toolCall;
    """.trimIndent()
}

internal fun buildExecutionRuntimeBridgeScript(): String {
    val preludeSource = JSONObject.quote(buildExecutionPreludeSource())
    return """
        (function() {
            var root = typeof globalThis !== 'undefined'
                ? globalThis
                : (typeof window !== 'undefined' ? window : this);
            if (typeof root.$TOOLPKG_EXECUTION_ENTRY_FUNCTION === 'function') {
                return;
            }

            var runtimePrelude = $preludeSource;

            function text(value) {
                return value == null ? '' : String(value);
            }

            function toBoolean(value) {
                if (value === true || value === false) {
                    return value;
                }
                if (typeof value === 'string') {
                    var normalized = value.trim().toLowerCase();
                    return normalized === 'true' || normalized === '1';
                }
                return !!value;
            }

            function hasUsableJavaInstanceMarker(value) {
                if (!value || typeof value !== 'object') {
                    return false;
                }
                try {
                    return (
                        Object.prototype.hasOwnProperty.call(value, '__javaHandle') &&
                        Object.prototype.hasOwnProperty.call(value, '__javaClass') &&
                        typeof value.__javaHandle === 'string' &&
                        typeof value.__javaClass === 'string' &&
                        text(value.__javaHandle).trim().length > 0 &&
                        text(value.__javaClass).trim().length > 0
                    );
                } catch (_javaMarkerError) {
                    return false;
                }
            }

            function normalizeSerializableValue(value, seen) {
                if (
                    value == null ||
                    typeof value === 'string' ||
                    typeof value === 'number' ||
                    typeof value === 'boolean'
                ) {
                    return value;
                }
                if (typeof value === 'bigint') {
                    return text(value);
                }
                if (typeof value === 'function') {
                    return text(value);
                }
                if (typeof value !== 'object') {
                    return text(value);
                }

                if (!seen) {
                    seen = [];
                }
                if (seen.indexOf(value) >= 0) {
                    return '[Circular]';
                }
                seen.push(value);

                try {
                    if (typeof value.toJSON === 'function') {
                        return normalizeSerializableValue(value.toJSON(), seen);
                    }

                    if (Array.isArray(value)) {
                        return value.map(function(item) {
                            return normalizeSerializableValue(item, seen);
                        });
                    }

                    if (hasUsableJavaInstanceMarker(value)) {
                        return {
                            __javaHandle: text(value.__javaHandle),
                            __javaClass: text(value.__javaClass)
                        };
                    }

                    var out = {};
                    var keys = Object.keys(value);
                    for (var i = 0; i < keys.length; i += 1) {
                        var key = keys[i];
                        out[key] = normalizeSerializableValue(value[key], seen);
                    }
                    return out;
                } finally {
                    seen.pop();
                }
            }

            function serializeOrThrow(value) {
                return JSON.stringify(normalizeSerializableValue(value, []));
            }

            function safeSerialize(value) {
                try {
                    return serializeOrThrow(value);
                } catch (error) {
                    return JSON.stringify({
                        error: 'Failed to serialize value',
                        message: text(error && error.message ? error.message : error),
                        value: text(value).slice(0, 1000)
                    });
                }
            }

            function getCallState(callId) {
                return typeof root.__operitGetCallState === 'function'
                    ? root.__operitGetCallState(callId)
                    : null;
            }

            function normalizePath(pathValue) {
                var parts = text(pathValue).replace(/\\/g, '/').split('/');
                var stack = [];
                for (var i = 0; i < parts.length; i += 1) {
                    var part = parts[i];
                    if (!part || part === '.') {
                        continue;
                    }
                    if (part === '..') {
                        if (stack.length > 0) {
                            stack.pop();
                        }
                        continue;
                    }
                    stack.push(part);
                }
                return stack.join('/');
            }

            function dirname(pathValue) {
                var normalized = normalizePath(pathValue);
                var index = normalized.lastIndexOf('/');
                return index < 0 ? '' : normalized.slice(0, index);
            }

            function resolveModulePath(request, fromPath) {
                var normalized = text(request).replace(/\\/g, '/').trim();
                if (!normalized) {
                    return '';
                }
                if (!(normalized.startsWith('.') || normalized.startsWith('/'))) {
                    return normalized;
                }
                if (normalized.startsWith('/')) {
                    return normalizePath(normalized);
                }
                var base = dirname(fromPath);
                return normalizePath(base ? base + '/' + normalized : normalized);
            }

            function buildCandidatePaths(modulePath) {
                var normalized = normalizePath(modulePath);
                if (!normalized) {
                    return [];
                }
                if (/\.[a-z0-9]+$/i.test(normalized)) {
                    return [normalized];
                }
                return [
                    normalized,
                    normalized + '.js',
                    normalized + '.json',
                    normalized + '/index.js',
                    normalized + '/index.json'
                ];
            }

            function hashText(value) {
                var textValue = text(value);
                var hash = 0;
                for (var i = 0; i < textValue.length; i += 1) {
                    hash = (((hash << 5) - hash) + textValue.charCodeAt(i)) | 0;
                }
                return (hash >>> 0).toString(16);
            }

            function ensureFactoryCache() {
                if (!root.__operitFactoryCache || typeof root.__operitFactoryCache !== 'object') {
                    root.__operitFactoryCache = Object.create(null);
                }
                return root.__operitFactoryCache;
            }

            function ensureModuleInstanceCache() {
                if (!root.__operitModuleInstanceCache || typeof root.__operitModuleInstanceCache !== 'object') {
                    root.__operitModuleInstanceCache = Object.create(null);
                }
                return root.__operitModuleInstanceCache;
            }

            function buildFactoryKey(kind, identity, source) {
                return [text(kind), text(identity), text(source).length, hashText(source)].join(':');
            }

            function buildModuleInstanceKey(kind, identity, source) {
                return ['instance', text(kind), text(identity), text(source).length, hashText(source)].join(':');
            }

            function createFactory(source) {
                return new Function(
                    'module',
                    'exports',
                    'require',
                    '__operit_call_runtime',
                    runtimePrelude + '\n' + source
                );
            }

            function getFactory(kind, identity, source) {
                var key = buildFactoryKey(kind, identity, source);
                var cache = ensureFactoryCache();
                if (typeof cache[key] === 'function') {
                    return cache[key];
                }
                var factory = createFactory(source);
                cache[key] = factory;
                return factory;
            }

            function tagModuleExports(modulePath, exportsRef) {
                if (typeof exportsRef === 'function') {
                    try { exportsRef.__operit_toolpkg_module_path = modulePath; } catch (_e) {}
                    return;
                }
                if (!exportsRef || typeof exportsRef !== 'object') {
                    return;
                }
                try { exportsRef.__operit_toolpkg_module_path = modulePath; } catch (_e) {}
                Object.keys(exportsRef).forEach(function(key) {
                    if (typeof exportsRef[key] === 'function') {
                        try { exportsRef[key].__operit_toolpkg_module_path = modulePath; } catch (_e) {}
                    }
                });
            }

            function createRegistrationScreenPlaceholder(modulePath) {
                function ScreenPlaceholder() {
                    return null;
                }
                try { ScreenPlaceholder.__operit_toolpkg_module_path = modulePath; } catch (_e) {}
                return ScreenPlaceholder;
            }

            function normalizeComposeResult(value) {
                if (!value || typeof value !== 'object' || !value.composeDsl || typeof value.composeDsl !== 'object') {
                    return value;
                }
                if (!Object.prototype.hasOwnProperty.call(value.composeDsl, 'screen')) {
                    return value;
                }
                var screenRef = value.composeDsl.screen;
                var resolved = '';
                if (typeof screenRef === 'function') {
                    resolved = text(screenRef.__operit_toolpkg_module_path).trim();
                } else if (
                    screenRef &&
                    typeof screenRef === 'object' &&
                    typeof screenRef.default === 'function'
                ) {
                    resolved = text(screenRef.default.__operit_toolpkg_module_path).trim();
                } else if (typeof screenRef === 'string') {
                    throw new Error('composeDsl.screen must be a compose_dsl screen function, not a string path');
                }
                if (!resolved) {
                    throw new Error('composeDsl.screen is missing a toolpkg module path marker');
                }
                value.composeDsl.screen = resolved.replace(/\\/g, '/');
                return value;
            }

            function findTargetFunction(exportsRef, moduleRef, functionName) {
                if (exportsRef && typeof exportsRef[functionName] === 'function') {
                    return exportsRef[functionName];
                }
                if (moduleRef && moduleRef.exports && typeof moduleRef.exports[functionName] === 'function') {
                    return moduleRef.exports[functionName];
                }
                if (typeof root[functionName] === 'function') {
                    return root[functionName];
                }
                return null;
            }

            function buildAvailableFunctions(exportsRef, moduleRef) {
                var names = [];
                function collect(target) {
                    if (!target || typeof target !== 'object') {
                        return;
                    }
                    Object.keys(target).forEach(function(key) {
                        if (typeof target[key] === 'function' && names.indexOf(key) < 0) {
                            names.push(key);
                        }
                    });
                }
                collect(exportsRef);
                collect(moduleRef && moduleRef.exports ? moduleRef.exports : null);
                return names;
            }

            root.$TOOLPKG_EXECUTION_ENTRY_FUNCTION = function(
                callId,
                params,
                scriptText,
                targetFunctionName,
                timeoutSec,
                preTimeoutMs
            ) {
                var registerCallSession =
                    typeof root.__operitRegisterCallSession === 'function'
                        ? root.__operitRegisterCallSession
                        : null;
                if (typeof registerCallSession !== 'function') {
                    NativeInterface.setCallError(
                        callId,
                        JSON.stringify({
                            success: false,
                            message: 'JS execution runtime bridge is unavailable'
                        })
                    );
                    return;
                }

                var safeTimeoutSec = Math.max(1, Number(timeoutSec) || 1);
                var safePreTimeoutMs = Math.max(1000, Number(preTimeoutMs) || 1000);
                var callState = registerCallSession(callId, params);
                var previousCallId = root.__operitCurrentCallId;
                var previousCallRuntime = root.__operit_call_runtime_ref;
                var previousDecodeGlobalBridgeTransferValue = root.__operitDecodeGlobalBridgeTransferValue;
                root.__operitCurrentCallId = callId;

                function markStage(stage) {
                    if (callState) {
                        callState.lastExecStage = text(stage);
                    }
                }

                function markFunction(name) {
                    if (callState) {
                        callState.lastExecFunction = text(name);
                    }
                }

                function markRequire(request, fromPath, resolvedPath) {
                    if (!callState) {
                        return;
                    }
                    callState.lastRequireRequest = text(request);
                    callState.lastRequireFrom = text(fromPath);
                    callState.lastRequireResolved = text(resolvedPath);
                }

                function markModule(modulePath) {
                    if (callState) {
                        callState.lastModulePath = text(modulePath);
                    }
                }

                function clearExecutionTimeouts() {
                    if (!callState) {
                        return;
                    }
                    try {
                        if (callState.safetyTimeout) clearTimeout(callState.safetyTimeout);
                        if (callState.safetyTimeoutFinal) clearTimeout(callState.safetyTimeoutFinal);
                    } catch (_e) {
                    }
                    callState.safetyTimeout = null;
                    callState.safetyTimeoutFinal = null;
                }

                function finalizeCall() {
                    clearExecutionTimeouts();
                    if (root.__operitCurrentCallId === callId) {
                        root.__operitCurrentCallId =
                            typeof previousCallId === 'string' ? previousCallId : '';
                    }
                    if (root.__operit_call_runtime_ref === callRuntime) {
                        if (
                            previousCallRuntime &&
                            typeof previousCallRuntime === 'object'
                        ) {
                            root.__operit_call_runtime_ref = previousCallRuntime;
                        } else {
                            try {
                                delete root.__operit_call_runtime_ref;
                            } catch (_deleteRuntimeError) {
                                root.__operit_call_runtime_ref = null;
                            }
                        }
                    }
                    if (root.__operitDecodeGlobalBridgeTransferValue === decodeGlobalBridgeTransferValue) {
                        if (typeof previousDecodeGlobalBridgeTransferValue === 'function') {
                            root.__operitDecodeGlobalBridgeTransferValue =
                                previousDecodeGlobalBridgeTransferValue;
                        } else {
                            try {
                                delete root.__operitDecodeGlobalBridgeTransferValue;
                            } catch (_deleteDecodeGlobalBridgeError) {
                                root.__operitDecodeGlobalBridgeTransferValue =
                                    previousDecodeGlobalBridgeTransferValue || null;
                            }
                        }
                    }
                    if (typeof root.__operitCleanupCallSession === 'function') {
                        root.__operitCleanupCallSession(callId);
                    }
                }

                function isActive() {
                    var state = getCallState(callId);
                    return !!(state && !state.completed);
                }

                function emitSerializedResult(resultText) {
                    var state = getCallState(callId);
                    if (!state || state.completed) {
                        return;
                    }
                    state.completed = true;
                    NativeInterface.setCallResult(callId, resultText);
                    finalizeCall();
                }

                function emitError(message) {
                    var state = getCallState(callId);
                    if (!state || state.completed) {
                        return;
                    }
                    state.completed = true;
                    NativeInterface.setCallError(
                        callId,
                        JSON.stringify({
                            success: false,
                            message: text(message)
                        })
                    );
                    finalizeCall();
                }

                function readCallValue(key, fallbackValue) {
                    var state = getCallState(callId);
                    var currentParams =
                        state && state.params && typeof state.params === 'object'
                            ? state.params
                            : null;
                    var value = currentParams ? currentParams[key] : undefined;
                    return value == null || value === '' ? fallbackValue : text(value);
                }

                callState.safetyTimeout = setTimeout(function() {
                    if (!isActive()) {
                        return;
                    }
                    callState.safetyTimeoutFinal = setTimeout(function() {
                        emitError('Script execution timed out after ' + safeTimeoutSec + ' seconds');
                    }, 5000);
                }, safePreTimeoutMs);

                function callRuntimeReport(error, context) {
                    if (typeof root.__operitReportDetailedErrorForCall === 'function') {
                        return root.__operitReportDetailedErrorForCall(callId, error, context);
                    }
                    return {
                        formatted: text(context) + ': ' + text(error),
                        details: { message: text(error), stack: text(error), lineNumber: 0 }
                    };
                }

                function emitIntermediate(value) {
                    if (isActive()) {
                        NativeInterface.sendCallIntermediateResult(callId, safeSerialize(value));
                    }
                }

                function complete(value) {
                    try {
                        emitSerializedResult(serializeOrThrow(normalizeComposeResult(value)));
                    } catch (error) {
                        var report = callRuntimeReport(error, 'Result Serialization Failure');
                        var serializationMessage =
                            report &&
                            report.details &&
                            typeof report.details.message === 'string' &&
                            report.details.message
                                ? report.details.message
                                : text(error && error.message ? error.message : error);
                        emitError('Result serialization failed: ' + serializationMessage);
                    }
                }

                function handleAsync(value) {
                    if (!value || typeof value.then !== 'function') {
                        return false;
                    }
                    Promise.resolve(value)
                        .then(function(result) {
                            if (isActive()) {
                                complete(result);
                            }
                        })
                        .catch(function(error) {
                            if (!isActive()) {
                                return;
                            }
                            var report = callRuntimeReport(error, 'Async Promise Rejection');
                            var rejectionMessage =
                                report &&
                                report.details &&
                                typeof report.details.message === 'string' &&
                                report.details.message
                                    ? report.details.message
                                    : text(error && error.message ? error.message : error);
                            emitError(rejectionMessage || 'Promise rejection');
                        });
                    return true;
                }

                function createRuntime() {
                    return {
                        emit: emitIntermediate,
                        delta: emitIntermediate,
                        log: emitIntermediate,
                        update: emitIntermediate,
                        sendIntermediateResult: emitIntermediate,
                        done: complete,
                        complete: complete,
                        getEnv: function(key) {
                            var value = NativeInterface.getEnvForCall(callId, text(key).trim());
                            return value == null || value === '' ? undefined : text(value);
                        },
                        getPluginConfigDir: function(pluginId) {
                            var explicitId = pluginId == null ? '' : text(pluginId).trim();
                            var resolvedId =
                                explicitId ||
                                readCallValue('__operit_ui_package_name', '') ||
                                readCallValue('toolPkgId', '') ||
                                readCallValue('containerPackageName', '') ||
                                readCallValue('__operit_package_name', '');
                            if (
                                !resolvedId ||
                                typeof NativeInterface === 'undefined' ||
                                !NativeInterface ||
                                typeof NativeInterface.getPluginConfigDir !== 'function'
                            ) {
                                return '';
                            }
                            var path = NativeInterface.getPluginConfigDir(resolvedId);
                            return typeof path === 'string' ? path : '';
                        },
                        getState: function() { return readCallValue('__operit_package_state', undefined); },
                        getLang: function() { return readCallValue('__operit_package_lang', 'en'); },
                        getCallerName: function() { return readCallValue('__operit_package_caller_name', undefined); },
                        getChatId: function() { return readCallValue('__operit_package_chat_id', undefined); },
                        getCallerCardId: function() { return readCallValue('__operit_package_caller_card_id', undefined); },
                        reportDetailedError: callRuntimeReport,
                        handleAsync: handleAsync,
                        console: {
                            log: function() { NativeInterface.logInfoForCall(callId, Array.prototype.slice.call(arguments).join(' ')); },
                            info: function() { NativeInterface.logInfoForCall(callId, Array.prototype.slice.call(arguments).join(' ')); },
                            warn: function() { NativeInterface.logInfoForCall(callId, Array.prototype.slice.call(arguments).join(' ')); },
                            error: function() { NativeInterface.logErrorForCall(callId, Array.prototype.slice.call(arguments).join(' ')); }
                        }
                    };
                }

                var callRuntime = createRuntime();
                root.__operit_call_runtime_ref = callRuntime;
                var registrationMode = toBoolean(readCallValue('__operit_registration_mode', false));
                var packageTarget =
                    readCallValue('__operit_ui_package_name', '') ||
                    readCallValue('toolPkgId', '');
                var screenPath = normalizePath(
                    readCallValue(
                        '__operit_script_screen',
                        params && params.moduleSpec && params.moduleSpec.screen
                            ? text(params.moduleSpec.screen)
                            : ''
                    )
                );
                var moduleCache = registrationMode
                    ? Object.create(null)
                    : ensureModuleInstanceCache();
                var globalRequiredModuleCache = Object.create(null);

                function readToolPkgModule(modulePath) {
                    if (
                        !packageTarget ||
                        typeof NativeInterface === 'undefined' ||
                        !NativeInterface ||
                        typeof NativeInterface.readToolPkgTextResource !== 'function'
                    ) {
                        return null;
                    }
                    var candidates = buildCandidatePaths(modulePath);
                    for (var i = 0; i < candidates.length; i += 1) {
                        var candidate = candidates[i];
                        var textResult = NativeInterface.readToolPkgTextResource(packageTarget, candidate);
                        if (typeof textResult === 'string' && textResult.length > 0) {
                            return { path: candidate, text: textResult };
                        }
                    }
                    return null;
                }

                function isUiModuleRuntime() {
                    var uiModuleId = text(
                        readCallValue(
                            '__operit_ui_module_id',
                            readCallValue('uiModuleId', '')
                        )
                    );
                    if (uiModuleId.length > 0) {
                        return true;
                    }
                    var contextKey = text(
                        readCallValue(
                            '__operit_compose_execution_context_key',
                            readCallValue('executionContextKey', '')
                        )
                    );
                    return contextKey.length > 0 && !/^toolpkg_main:/i.test(contextKey);
                }

                function isLocalUiModulePath(modulePath) {
                    return /\.ui\.js$/i.test(normalizePath(modulePath));
                }

                function parseGlobalModuleBridgeResponse(raw, actionLabel) {
                    if (typeof raw !== 'string' || raw.trim().length === 0) {
                        throw new Error(actionLabel + ' returned empty response');
                    }
                    var parsed;
                    try {
                        parsed = JSON.parse(raw);
                    } catch (error) {
                        throw new Error(
                            actionLabel +
                                ' returned invalid JSON: ' +
                                text(error && error.message ? error.message : error)
                        );
                    }
                    if (!parsed || parsed.success !== true) {
                        var message =
                            parsed &&
                            typeof parsed.message === 'string' &&
                            parsed.message.trim().length > 0
                                ? parsed.message.trim()
                                : '';
                        throw new Error(message);
                    }
                    return parsed;
                }

                function createGlobalBridgeSourceForModule(modulePath) {
                    return {
                        type: 'module',
                        id: normalizePath(modulePath)
                    };
                }

                function createGlobalBridgeSourceForHandle(handleId, contextKey) {
                    return {
                        type: 'handle',
                        id: text(handleId).trim(),
                        contextKey: text(contextKey).trim()
                    };
                }

                function getToolPkgBridgeStore() {
                    var store = root.__operitToolPkgBridgeReturnStore;
                    if (!store || typeof store !== 'object') {
                        store = {
                            nextId: 1,
                            values: Object.create(null),
                            objectIds: typeof WeakMap === 'function' ? new WeakMap() : null
                        };
                        root.__operitToolPkgBridgeReturnStore = store;
                    }
                    if (!store.values || typeof store.values !== 'object') {
                        store.values = Object.create(null);
                    }
                    if (!store.objectIds && typeof WeakMap === 'function') {
                        store.objectIds = new WeakMap();
                    }
                    store.nextId = Number(store.nextId) || 1;
                    return store;
                }

                function storeToolPkgBridgeValue(value) {
                    if (value == null) {
                        return '';
                    }
                    var valueType = typeof value;
                    if (valueType !== 'object' && valueType !== 'function') {
                        return '';
                    }
                    var store = getToolPkgBridgeStore();
                    var existingId = '';
                    if (store.objectIds && typeof store.objectIds.get === 'function') {
                        existingId = text(store.objectIds.get(value)).trim();
                    }
                    if (existingId && store.values[existingId]) {
                        return existingId;
                    }
                    var handleId = 'bridge_' + String(store.nextId++);
                    store.values[handleId] = value;
                    if (store.objectIds && typeof store.objectIds.set === 'function') {
                        store.objectIds.set(value, handleId);
                    }
                    return handleId;
                }

                function buildMainToolPkgExecutionContextKey() {
                    return packageTarget ? 'toolpkg_main:' + packageTarget : '';
                }

                function getCurrentToolPkgExecutionContextKey() {
                    var composeContextKey = text(
                        readCallValue(
                            '__operit_compose_execution_context_key',
                            readCallValue('executionContextKey', '')
                        )
                    ).trim();
                    var scopedContextKey = text(
                        readCallValue('__operit_execution_context_key', '')
                    ).trim();
                    if (composeContextKey.length > 0) {
                        return composeContextKey;
                    }
                    if (scopedContextKey.length > 0) {
                        return scopedContextKey;
                    }
                    return buildMainToolPkgExecutionContextKey();
                }

                function isHandleGlobalBridgeSource(source) {
                    return !!source && source.type === 'handle';
                }

                function getGlobalBridgeSourceContextKey(source) {
                    return isHandleGlobalBridgeSource(source)
                        ? text(source && source.contextKey).trim()
                        : '';
                }

                function getGlobalBridgeNativeMethodName(action, source) {
                    if (action === 'read') {
                        return isHandleGlobalBridgeSource(source)
                            ? 'readGlobalToolPkgHandleMember'
                            : 'readGlobalToolPkgModuleMember';
                    }
                    if (action === 'invoke') {
                        return isHandleGlobalBridgeSource(source)
                            ? 'invokeGlobalToolPkgHandleFunction'
                            : 'invokeGlobalToolPkgModuleFunction';
                    }
                    if (action === 'write') {
                        return isHandleGlobalBridgeSource(source)
                            ? 'writeGlobalToolPkgHandleMember'
                            : 'writeGlobalToolPkgModuleMember';
                    }
                    throw new Error('unsupported global bridge action: ' + text(action));
                }

                function normalizeGlobalBridgeMemberPath(memberPath) {
                    return Array.isArray(memberPath)
                        ? memberPath.map(function(item) { return String(item); })
                        : [];
                }

                function getGlobalBridgeSourceId(source) {
                    return isHandleGlobalBridgeSource(source)
                        ? text(source && source.id).trim()
                        : normalizePath(source && source.id);
                }

                function getGlobalBridgeKind(descriptor) {
                    if (!descriptor || typeof descriptor !== 'object') {
                        return 'object';
                    }
                    if (descriptor.kind === 'function') {
                        return 'function';
                    }
                    if (descriptor.kind === 'array') {
                        return 'array';
                    }
                    return 'object';
                }

                function normalizeGlobalHandleKind(handleKind) {
                    var normalized = text(handleKind).trim().toLowerCase();
                    if (
                        normalized === 'function' ||
                        normalized === 'array' ||
                        normalized === 'object'
                    ) {
                        return normalized;
                    }
                    return 'object';
                }

                function readGlobalBridgeHandleKind(value) {
                    if (!value || (typeof value !== 'object' && typeof value !== 'function')) {
                        return '';
                    }
                    try {
                        var handleKind = text(
                            value.__operit_toolpkg_bridge_handle_kind
                        ).trim();
                        if (handleKind) {
                            return normalizeGlobalHandleKind(handleKind);
                        }
                    } catch (_readHandleKindError) {
                    }
                    return '';
                }

                function readGlobalBridgeHandleContextKey(value) {
                    if (!value || (typeof value !== 'object' && typeof value !== 'function')) {
                        return '';
                    }
                    try {
                        return text(
                            value.__operit_toolpkg_bridge_context_key
                        ).trim();
                    } catch (_readHandleContextError) {
                    }
                    return '';
                }

                function objectHasOwnFunctionValues(value, keys) {
                    var normalizedKeys = Array.isArray(keys) ? keys : [];
                    for (var i = 0; i < normalizedKeys.length; i += 1) {
                        try {
                            if (typeof value[normalizedKeys[i]] === 'function') {
                                return true;
                            }
                        } catch (_readError) {
                        }
                    }
                    return false;
                }

                function shouldUseHandleTransferForObject(value, keys) {
                    return !canSerializeAsPlainObject(value) || objectHasOwnFunctionValues(value, keys);
                }

                function encodeGlobalBridgeTransferValue(value, seen) {
                    if (value === undefined) {
                        return { kind: 'undefined' };
                    }
                    if (value === null) {
                        return { kind: 'null' };
                    }
                    if (
                        typeof value === 'string' ||
                        typeof value === 'number' ||
                        typeof value === 'boolean'
                    ) {
                        return {
                            kind: 'primitive',
                            value: value
                        };
                    }
                    if (typeof value === 'bigint') {
                        return {
                            kind: 'primitive',
                            value: text(value)
                        };
                    }
                    if (hasUsableJavaInstanceMarker(value)) {
                        return {
                            kind: 'javaHandle',
                            value: {
                                __javaHandle: text(value.__javaHandle),
                                __javaClass: text(value.__javaClass)
                            }
                        };
                    }
                    if (
                        value &&
                        typeof value === 'object' &&
                        typeof value.__operit_toolpkg_module_path === 'string' &&
                        value.__operit_toolpkg_module_path.trim().length > 0
                    ) {
                        return {
                            kind: 'globalModule',
                            modulePath: normalizePath(value.__operit_toolpkg_module_path)
                        };
                    }
                    if (
                        value &&
                        typeof value === 'object' &&
                        typeof value.__operit_toolpkg_bridge_handle_id === 'string' &&
                        value.__operit_toolpkg_bridge_handle_id.trim().length > 0
                    ) {
                        var existingHandleId = text(value.__operit_toolpkg_bridge_handle_id).trim();
                        var existingHandleContextKey = readGlobalBridgeHandleContextKey(value);
                        if (!existingHandleContextKey) {
                            throw new Error('global bridge handle context key is missing');
                        }
                        return {
                            kind: 'globalHandle',
                            handleId: existingHandleId,
                            contextKey: existingHandleContextKey,
                            handleKind: readGlobalBridgeHandleKind(value)
                        };
                    }
                    if (typeof value === 'function') {
                        return {
                            kind: 'globalHandle',
                            handleId: storeToolPkgBridgeValue(value),
                            contextKey: getCurrentToolPkgExecutionContextKey(),
                            handleKind: 'function'
                        };
                    }
                    if (!value || typeof value !== 'object') {
                        return {
                            kind: 'primitive',
                            value: text(value)
                        };
                    }
                    if (!seen) {
                        seen = [];
                    }
                    if (seen.indexOf(value) >= 0) {
                        return {
                            kind: 'globalHandle',
                            handleId: storeToolPkgBridgeValue(value),
                            contextKey: getCurrentToolPkgExecutionContextKey(),
                            handleKind: Array.isArray(value) ? 'array' : 'object'
                        };
                    }
                    seen.push(value);
                    try {
                        if (Array.isArray(value)) {
                            return {
                                kind: 'array',
                                items: value.map(function(item) {
                                    return encodeGlobalBridgeTransferValue(item, seen);
                                })
                            };
                        }
                        var keys = Object.keys(value);
                        if (shouldUseHandleTransferForObject(value, keys)) {
                            return {
                                kind: 'globalHandle',
                                handleId: storeToolPkgBridgeValue(value),
                                contextKey: getCurrentToolPkgExecutionContextKey(),
                                handleKind: Array.isArray(value) ? 'array' : 'object'
                            };
                        }
                        var out = {};
                        for (var i = 0; i < keys.length; i += 1) {
                            var key = keys[i];
                            out[key] = encodeGlobalBridgeTransferValue(value[key], seen);
                        }
                        return {
                            kind: 'object',
                            value: out
                        };
                    } finally {
                        seen.pop();
                    }
                }

                function decodeGlobalBridgeTransferValue(value) {
                    if (!value || typeof value !== 'object') {
                        return undefined;
                    }
                    if (value.kind === 'undefined') {
                        return undefined;
                    }
                    if (value.kind === 'null') {
                        return null;
                    }
                    if (value.kind === 'primitive') {
                        return value.value;
                    }
                    if (value.kind === 'javaHandle') {
                        var wrapValue =
                            typeof root.__operitJavaBridgeWrapValue === 'function'
                                ? root.__operitJavaBridgeWrapValue
                                : null;
                        return wrapValue
                            ? wrapValue(value.value)
                            : value.value;
                    }
                    if (value.kind === 'globalModule') {
                        return require('/' + normalizePath(value.modulePath));
                    }
                    if (value.kind === 'globalHandle') {
                        return buildGlobalBridgeValue(
                            createGlobalBridgeSourceForHandle(
                                value.handleId,
                                value.contextKey
                            ),
                            [],
                            {
                                kind: normalizeGlobalHandleKind(value.handleKind)
                            }
                        );
                    }
                    if (value.kind === 'array') {
                        return Array.isArray(value.items)
                            ? value.items.map(function(item) {
                                return decodeGlobalBridgeTransferValue(item);
                            })
                            : [];
                    }
                    if (value.kind === 'object') {
                        var out = {};
                        var keys = value.value && typeof value.value === 'object'
                            ? Object.keys(value.value)
                            : [];
                        for (var i = 0; i < keys.length; i += 1) {
                            var key = keys[i];
                            out[key] = decodeGlobalBridgeTransferValue(value.value[key]);
                        }
                        return out;
                    }
                    return undefined;
                }

                function callGlobalBridge(action, source, memberPath, payload) {
                    var methodName = getGlobalBridgeNativeMethodName(action, source);
                    if (
                        !packageTarget ||
                        typeof NativeInterface === 'undefined' ||
                        !NativeInterface ||
                        typeof NativeInterface[methodName] !== 'function'
                    ) {
                        throw new Error('NativeInterface.' + methodName + ' is unavailable');
                    }
                    var normalizedSourceId = getGlobalBridgeSourceId(source);
                    var normalizedSourceContextKey = getGlobalBridgeSourceContextKey(source);
                    var normalizedMemberPath = JSON.stringify(
                        normalizeGlobalBridgeMemberPath(memberPath)
                    );
                    var args = isHandleGlobalBridgeSource(source)
                        ? [packageTarget, normalizedSourceContextKey, normalizedSourceId, normalizedMemberPath]
                        : [packageTarget, normalizedSourceId, normalizedMemberPath];
                    if (isHandleGlobalBridgeSource(source) && !normalizedSourceContextKey) {
                        throw new Error('global bridge handle source context key is empty');
                    }
                    if (action === 'invoke') {
                        var payloadArgs = Array.isArray(payload) ? payload : [];
                        var encodedPayloadArgs = payloadArgs.map(function(item) {
                            return encodeGlobalBridgeTransferValue(item, []);
                        });
                        args.push(
                            JSON.stringify(encodedPayloadArgs)
                        );
                    } else if (action === 'write') {
                        var serializedValue = JSON.stringify(
                            encodeGlobalBridgeTransferValue(payload, [])
                        );
                        args.push(typeof serializedValue === 'string' ? serializedValue : 'null');
                    }
                    return parseGlobalModuleBridgeResponse(
                        NativeInterface[methodName].apply(NativeInterface, args),
                        methodName + '(' + normalizedSourceId + ')'
                    );
                }

                function readGlobalBridgeMember(source, memberPath) {
                    return callGlobalBridge('read', source, memberPath);
                }

                function invokeGlobalBridgeFunction(source, memberPath, argsArray) {
                    return callGlobalBridge('invoke', source, memberPath, argsArray);
                }

                function writeGlobalBridgeMember(source, memberPath, value) {
                    return callGlobalBridge('write', source, memberPath, value);
                }

                function materializeGlobalInvocationResult(source, result) {
                    if (!result || typeof result !== 'object') {
                        return undefined;
                    }
                    if (result.kind === 'undefined') {
                        return undefined;
                    }
                    if (result.kind === 'null') {
                        return null;
                    }
                    if (
                        (result.kind === 'function' ||
                            result.kind === 'object' ||
                            result.kind === 'array') &&
                        typeof result.handleId === 'string' &&
                        result.handleId.trim().length > 0
                    ) {
                        var resultContextKey = isHandleGlobalBridgeSource(source)
                            ? getGlobalBridgeSourceContextKey(source)
                            : buildMainToolPkgExecutionContextKey();
                        if (!resultContextKey) {
                            throw new Error('global bridge handle result context key is empty');
                        }
                        var handleSource = createGlobalBridgeSourceForHandle(
                            result.handleId,
                            resultContextKey
                        );
                        return buildGlobalBridgeValue(handleSource, [], result);
                    }
                    if (result.kind === 'primitive') {
                        return result.value;
                    }
                    throw new Error(
                        'unsupported global toolpkg invocation result kind: ' +
                        text(result.kind || 'unknown')
                    );
                }

                function materializeGlobalSnapshot(source, memberPath, descriptor) {
                    if (arguments.length < 3) {
                        descriptor = readGlobalBridgeMember(source, memberPath);
                    }
                    if (!descriptor || typeof descriptor !== 'object') {
                        return undefined;
                    }
                    if (descriptor.kind === 'undefined') {
                        return undefined;
                    }
                    if (descriptor.kind === 'null') {
                        return null;
                    }
                    if (descriptor.kind === 'primitive') {
                        return descriptor.value;
                    }

                    var kind = getGlobalBridgeKind(descriptor);
                    var normalizedMemberPath = normalizeGlobalBridgeMemberPath(memberPath);
                    var keys = Array.isArray(descriptor.keys) ? descriptor.keys : [];
                    var out = kind === 'array' ? [] : {};
                    for (var i = 0; i < keys.length; i += 1) {
                        var key = String(keys[i]);
                        out[key] = materializeGlobalSnapshot(
                            source,
                            normalizedMemberPath.concat([key])
                        );
                    }
                    if (kind === 'array') {
                        var nextLength = Number(descriptor.length);
                        if (!isNaN(nextLength) && nextLength >= 0) {
                            out.length = nextLength;
                        }
                    }
                    return out;
                }

                function nextGlobalModuleBridgeCallbackId() {
                    return (
                        '__operit_global_bridge_' +
                        Date.now() +
                        '_' +
                        Math.random().toString(36).slice(2, 10)
                    );
                }

                function invokeGlobalToolPkgBridgeAsync(source, actionLabel, nativeInvoker) {
                    return new Promise(function(resolve, reject) {
                        var callbackId = nextGlobalModuleBridgeCallbackId();
                        root[callbackId] = function(rawResponse) {
                            delete root[callbackId];
                            try {
                                resolve(
                                    materializeGlobalInvocationResult(
                                        source,
                                        parseGlobalModuleBridgeResponse(rawResponse, actionLabel)
                                    )
                                );
                            } catch (error) {
                                reject(error);
                            }
                        };
                        try {
                            nativeInvoker(callbackId);
                        } catch (error) {
                            delete root[callbackId];
                            reject(error);
                        }
                    });
                }

                function invokeGlobalBridgeFunctionAsync(source, memberPath, argsArray) {
                    var methodName = isHandleGlobalBridgeSource(source)
                        ? 'invokeGlobalToolPkgHandleFunctionAsync'
                        : 'invokeGlobalToolPkgModuleFunctionAsync';
                    var normalizedMemberPath = JSON.stringify(
                        normalizeGlobalBridgeMemberPath(memberPath)
                    );
                    var payloadArgs = Array.isArray(argsArray) ? argsArray : [];
                    var encodedPayloadArgs = payloadArgs.map(function(item) {
                        return encodeGlobalBridgeTransferValue(item, []);
                    });
                    var serializedArgs = JSON.stringify(encodedPayloadArgs);
                    if (
                        !packageTarget ||
                        typeof NativeInterface === 'undefined' ||
                        !NativeInterface ||
                        typeof NativeInterface[methodName] !== 'function'
                    ) {
                        throw new Error('NativeInterface.' + methodName + ' is unavailable');
                    }
                    var normalizedSourceId = getGlobalBridgeSourceId(source);
                    var normalizedSourceContextKey = getGlobalBridgeSourceContextKey(source);
                    return invokeGlobalToolPkgBridgeAsync(
                        source,
                        methodName.replace(/Async$/, '') + '(' + normalizedSourceId + ')',
                        function(callbackId) {
                            NativeInterface[methodName](
                                callbackId,
                                packageTarget,
                                normalizedSourceContextKey,
                                normalizedSourceId,
                                normalizedMemberPath,
                                serializedArgs
                            );
                        }
                    );
                }

                function buildGlobalBridgeValue(source, memberPath, descriptor) {
                    if (!descriptor || typeof descriptor !== 'object') {
                        return undefined;
                    }
                    if (descriptor.kind === 'undefined') {
                        return undefined;
                    }
                    if (descriptor.kind === 'null') {
                        return null;
                    }
                    if (descriptor.kind === 'primitive') {
                        return descriptor.value;
                    }

                    var kind = getGlobalBridgeKind(descriptor);
                    var normalizedMemberPath = normalizeGlobalBridgeMemberPath(memberPath);
                    var normalizedSourceId = getGlobalBridgeSourceId(source);
                    var normalizedSourceContextKey = getGlobalBridgeSourceContextKey(source);
                    var sourceIdentity = normalizedSourceId;
                    if (isHandleGlobalBridgeSource(source)) {
                        sourceIdentity =
                            normalizedSourceContextKey +
                            '@@' +
                            sourceIdentity;
                    }
                    var cacheKey = (
                        (isHandleGlobalBridgeSource(source) ? 'handle' : 'module') +
                        '::' +
                        sourceIdentity +
                        '::' +
                        JSON.stringify(normalizedMemberPath) +
                        '::' +
                        text(descriptor.kind)
                    );
                    if (globalRequiredModuleCache[cacheKey]) {
                        return globalRequiredModuleCache[cacheKey];
                    }

                    var isAsyncFunction = kind === 'function' && descriptor.isAsync === true;
                    var invokeFn = function(args) {
                        if (isAsyncFunction) {
                            return invokeGlobalBridgeFunctionAsync(
                                source,
                                normalizedMemberPath,
                                args
                            );
                        }
                        return materializeGlobalInvocationResult(
                            source,
                            invokeGlobalBridgeFunction(
                                source,
                                normalizedMemberPath,
                                args
                            )
                        );
                    };
                    var target = kind === 'function'
                        ? function() {
                            return invokeFn(Array.prototype.slice.call(arguments));
                        }
                        : (kind === 'array' ? [] : {});

                    var proxy = new Proxy(target, {
                        get: function(proxyTarget, prop, receiver) {
                            if (typeof prop === 'symbol') {
                                if (prop === Symbol.toStringTag) {
                                    return kind === 'array' ? 'Array' : (kind === 'function' ? 'Function' : 'Object');
                                }
                                if (kind === 'array' && prop === Symbol.iterator) {
                                    return function() {
                                        return materializeGlobalSnapshot(source, normalizedMemberPath)[Symbol.iterator]();
                                    };
                                }
                                return Reflect.get(proxyTarget, prop, receiver);
                            }
                            if (prop === 'then') {
                                return undefined;
                            }
                            if (
                                prop === (
                                    isHandleGlobalBridgeSource(source)
                                        ? '__operit_toolpkg_bridge_handle_id'
                                        : '__operit_toolpkg_module_path'
                                )
                            ) {
                                return normalizedSourceId;
                            }
                            if (
                                isHandleGlobalBridgeSource(source) &&
                                prop === '__operit_toolpkg_bridge_context_key'
                            ) {
                                return normalizedSourceContextKey;
                            }
                            if (
                                isHandleGlobalBridgeSource(source) &&
                                prop === '__operit_toolpkg_bridge_handle_kind'
                            ) {
                                return kind;
                            }
                            if (kind === 'function' && (
                                prop === 'name' ||
                                prop === 'length' ||
                                prop === 'prototype' ||
                                prop === 'caller' ||
                                prop === 'arguments'
                            )) {
                                return Reflect.get(proxyTarget, prop, receiver);
                            }
                            if (kind !== 'function' && prop === 'toJSON') {
                                return function() {
                                    return materializeGlobalSnapshot(source, normalizedMemberPath);
                                };
                            }
                            if (
                                kind === 'array' &&
                                typeof prop === 'string' &&
                                prop !== 'constructor' &&
                                typeof Array.prototype[prop] === 'function'
                            ) {
                                return function() {
                                    var snapshot = materializeGlobalSnapshot(
                                        source,
                                        normalizedMemberPath
                                    );
                                    var localMethod = snapshot && snapshot[prop];
                                    if (typeof localMethod !== 'function') {
                                        throw new Error(
                                            'array bridge method is unavailable locally: ' +
                                                String(prop)
                                        );
                                    }
                                    return localMethod.apply(
                                        snapshot,
                                        Array.prototype.slice.call(arguments)
                                    );
                                };
                            }

                            var nextDescriptor = readGlobalBridgeMember(
                                source,
                                normalizedMemberPath.concat([String(prop)])
                            );
                            return buildGlobalBridgeValue(
                                source,
                                normalizedMemberPath.concat([String(prop)]),
                                nextDescriptor
                            );
                        },
                        set: function(proxyTarget, prop, value) {
                            if (typeof prop !== 'string') {
                                return false;
                            }
                            if (kind === 'function' && (
                                prop === 'name' ||
                                prop === 'length' ||
                                prop === 'prototype' ||
                                prop === 'caller' ||
                                prop === 'arguments'
                            )) {
                                return Reflect.set(proxyTarget, prop, value);
                            }
                            writeGlobalBridgeMember(
                                source,
                                normalizedMemberPath.concat([String(prop)]),
                                value
                            );
                            return true;
                        },
                        ownKeys: function(proxyTarget) {
                            var latestDescriptor = readGlobalBridgeMember(source, normalizedMemberPath);
                            var keys = Reflect.ownKeys(proxyTarget);
                            if (Array.isArray(latestDescriptor.keys)) {
                                latestDescriptor.keys.forEach(function(key) {
                                    var normalizedKey = String(key);
                                    if (keys.indexOf(normalizedKey) < 0) {
                                        keys.push(normalizedKey);
                                    }
                                });
                            }
                            if (kind === 'array' && keys.indexOf('length') < 0) {
                                keys.push('length');
                            }
                            return keys;
                        },
                        has: function(_proxyTarget, prop) {
                            if (typeof prop !== 'string') {
                                return false;
                            }
                            if (kind === 'array' && prop === 'length') {
                                return true;
                            }
                            var latestDescriptor = readGlobalBridgeMember(source, normalizedMemberPath);
                            return Array.isArray(latestDescriptor.keys)
                                ? latestDescriptor.keys.map(String).indexOf(prop) >= 0
                                : false;
                        },
                        getOwnPropertyDescriptor: function(proxyTarget, prop) {
                            var localDescriptor = Reflect.getOwnPropertyDescriptor(proxyTarget, prop);
                            if (localDescriptor) {
                                return localDescriptor;
                            }
                            if (typeof prop !== 'string') {
                                return undefined;
                            }
                            return {
                                enumerable: true,
                                configurable: true
                            };
                        },
                        apply: kind === 'function'
                            ? function(_proxyTarget, _thisArg, argList) {
                                return invokeFn(Array.isArray(argList) ? argList : []);
                            }
                            : undefined
                    });

                    globalRequiredModuleCache[cacheKey] = proxy;
                    return proxy;
                }

                function canSerializeAsPlainObject(value) {
                    if (!value || typeof value !== 'object') {
                        return false;
                    }
                    if (Array.isArray(value)) {
                        return true;
                    }
                    var prototype = Object.getPrototypeOf(value);
                    return prototype === Object.prototype || prototype === null;
                }

                root.__operitDecodeGlobalBridgeTransferValue = decodeGlobalBridgeTransferValue;

                function executeModule(modulePath, moduleText, requireInternal) {
                    markStage('execute_required_module');
                    markModule(modulePath);

                    var moduleKey = buildModuleInstanceKey('module', packageTarget + ':' + modulePath, moduleText);
                    if (moduleCache[moduleKey]) {
                        return moduleCache[moduleKey].exports;
                    }

                    var module = { exports: {} };
                    moduleCache[moduleKey] = module;

                    if (/\.json$/i.test(modulePath)) {
                        try {
                            module.exports = JSON.parse(moduleText);
                            return module.exports;
                        } catch (error) {
                            delete moduleCache[moduleKey];
                            throw error;
                        }
                    }

                    var localRequire = function(nextName) {
                        return requireInternal(nextName, modulePath);
                    };
                    var factory = getFactory('module', packageTarget + ':' + modulePath, moduleText);
                    var previousActiveModule = root.__operitActiveModule;
                    var previousActiveExports = root.__operitActiveModuleExports;
                    root.__operitActiveModule = module;
                    root.__operitActiveModuleExports = module.exports;
                    try {
                        factory(module, module.exports, localRequire, callRuntime);
                    } catch (error) {
                        delete moduleCache[moduleKey];
                        throw error;
                    } finally {
                        root.__operitActiveModule = previousActiveModule;
                        root.__operitActiveModuleExports = previousActiveExports;
                    }
                    tagModuleExports(modulePath, module.exports);
                    return module.exports;
                }

                function requireInternal(moduleName, fromPath) {
                    var request = text(moduleName).trim();
                    if (request === 'lodash') {
                        return root._;
                    }
                    if (request === 'uuid') {
                        return {
                            v4: function() {
                                return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(char) {
                                    var random = Math.random() * 16 | 0;
                                    var value = char === 'x' ? random : ((random & 0x3) | 0x8);
                                    return value.toString(16);
                                });
                            }
                        };
                    }
                    if (request === 'axios') {
                        return {
                            get: function(url, config) {
                                return root.toolCall('http_request', config ? Object.assign({ url: url }, config) : { url: url });
                            },
                            post: function(url, data, config) {
                                return root.toolCall('http_request', config ? Object.assign({ url: url, data: data }, config) : { url: url, data: data });
                            }
                        };
                    }
                    if (!(request.startsWith('.') || request.startsWith('/'))) {
                        return {};
                    }

                    var resolvedPath = resolveModulePath(request, fromPath || screenPath);
                    markStage('require_module');
                    markRequire(request, fromPath || screenPath || '<root>', resolvedPath);
                    markModule(resolvedPath);

                    if (registrationMode && isLocalUiModulePath(resolvedPath)) {
                        return createRegistrationScreenPlaceholder(resolvedPath);
                    }

                    if (isUiModuleRuntime() && !isLocalUiModulePath(resolvedPath)) {
                        var globalModuleCacheKey = 'global:' + resolvedPath;
                        if (Object.prototype.hasOwnProperty.call(globalRequiredModuleCache, globalModuleCacheKey)) {
                            return globalRequiredModuleCache[globalModuleCacheKey];
                        }
                        var globalDescriptor = readGlobalBridgeMember(
                            createGlobalBridgeSourceForModule(resolvedPath),
                            []
                        );
                        var globalValue = buildGlobalBridgeValue(
                            createGlobalBridgeSourceForModule(resolvedPath),
                            [],
                            globalDescriptor
                        );
                        globalRequiredModuleCache[globalModuleCacheKey] = globalValue;
                        return globalValue;
                    }

                    var loaded = readToolPkgModule(resolvedPath);
                    if (!loaded) {
                        throw new Error(
                            'Cannot resolve module "' + request + '" from "' + (fromPath || screenPath || '<root>') + '"'
                        );
                    }
                    return executeModule(loaded.path, loaded.text, requireInternal);
                }

                try {
                    markFunction(targetFunctionName);
                    var mainModuleIdentity = packageTarget + ':' + (screenPath || '<root>');
                    var mainModuleKey = buildModuleInstanceKey('main', mainModuleIdentity, scriptText);
                    var module = moduleCache[mainModuleKey];
                    var exports = module && module.exports ? module.exports : null;
                    var require = function(moduleName) {
                        markStage('require_request');
                        markRequire(moduleName, screenPath || '<root>', '');
                        return requireInternal(moduleName, screenPath);
                    };

                    if (!module) {
                        module = { exports: {} };
                        moduleCache[mainModuleKey] = module;
                        exports = module.exports;
                        markStage('compile_main_script');
                        var mainFactory = getFactory('main', packageTarget + ':' + screenPath, scriptText);
                        markStage('execute_main_script');
                        var previousActiveModule = root.__operitActiveModule;
                        var previousActiveExports = root.__operitActiveModuleExports;
                        root.__operitActiveModule = module;
                        root.__operitActiveModuleExports = exports;
                        try {
                            mainFactory(module, exports, require, callRuntime);
                        } catch (error) {
                            delete moduleCache[mainModuleKey];
                            throw error;
                        } finally {
                            root.__operitActiveModule = previousActiveModule;
                            root.__operitActiveModuleExports = previousActiveExports;
                        }
                    } else {
                        if (exports == null) {
                            exports = {};
                            module.exports = exports;
                        }
                        markStage('reuse_main_script');
                    }
                    var rootExports = module.exports || exports || {};
                    tagModuleExports(screenPath || '<root>', rootExports);

                    var inlineFunctionName = readCallValue('__operit_inline_function_name', '');
                    var inlineFunctionSource = readCallValue('__operit_inline_function_source', '');
                    if (inlineFunctionName && inlineFunctionSource) {
                        markStage('evaluate_inline_hook_function');
                        var inlineFunction = eval('(' + inlineFunctionSource + ')');
                        if (typeof inlineFunction !== 'function') {
                            throw new Error('inline hook source did not evaluate to function');
                        }
                        rootExports[inlineFunctionName] = inlineFunction;
                        module.exports[inlineFunctionName] = inlineFunction;
                    }

                    var targetFunction = findTargetFunction(rootExports, module, targetFunctionName);
                    if (typeof targetFunction !== 'function') {
                        emitError(
                            "Function '" +
                                targetFunctionName +
                                "' not found in script. Available functions: " +
                                buildAvailableFunctions(rootExports, module).join(', ')
                        );
                        return;
                    }

                    markStage('invoke_target_function');
                    var previousModule = callState.currentModule;
                    var previousExports = callState.currentModuleExports;
                    var previousActiveModule = root.__operitActiveModule;
                    var previousActiveExports = root.__operitActiveModuleExports;
                    callState.currentModule = module;
                    callState.currentModuleExports = rootExports;
                    root.__operitActiveModule = module;
                    root.__operitActiveModuleExports = rootExports;
                    var functionResult;
                    try {
                        functionResult = targetFunction(params);
                    } finally {
                        callState.currentModule = previousModule;
                        callState.currentModuleExports = previousExports;
                        root.__operitActiveModule = previousActiveModule;
                        root.__operitActiveModuleExports = previousActiveExports;
                    }

                    markStage('handle_function_result');
                    if (!handleAsync(functionResult)) {
                        complete(functionResult);
                    }
                } catch (error) {
                    var runtimeContext = typeof root.__operitBuildRuntimeContext === 'function'
                        ? text(root.__operitBuildRuntimeContext(callId))
                        : '';
                    emitError(
                        'Script error: ' +
                            text(error && error.message ? error.message : error) +
                            (runtimeContext ? '\nRuntime Context: ' + runtimeContext : '') +
                            (error && error.stack ? '\nStack: ' + text(error.stack) : '')
                    );
                }
            };
        })();
    """.trimIndent()
}
