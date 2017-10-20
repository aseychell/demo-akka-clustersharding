package com.ixaris.academy.akka.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

public final class ProtobufHelper {

    public static String toJson(final MessageOrBuilder msg) throws InvalidProtocolBufferException {
        return JsonFormat.printer().includingDefaultValueFields().print(msg);
    }

    private ProtobufHelper() {}
}
