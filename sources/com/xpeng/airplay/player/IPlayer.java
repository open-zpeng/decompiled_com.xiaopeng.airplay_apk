package com.xpeng.airplay.player;
/* loaded from: classes.dex */
public interface IPlayer<E> {
    void addPacket(E e);

    void startPlay();

    void stopPlay();
}
