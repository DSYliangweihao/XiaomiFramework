package com.miui.antispam.service.backup;

import com.google.protobuf.AbstractParser;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

class A extends AbstractParser<B> {
    A() {
    }

    public B parsePartialFrom(CodedInputStream codedInputStream, ExtensionRegistryLite extensionRegistryLite) {
        return new B(codedInputStream, extensionRegistryLite);
    }
}