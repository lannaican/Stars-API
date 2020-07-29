package com.star.api.socket;

/**
 * 连接状态
 */
public enum SocketState {
    None,
    Connecting,
    Connected,
    Reconnecting,
    ConnectFail,
    Disconnect,
    Close,
}
