package com.miui.antispam.policy;

import android.content.Context;
import android.text.TextUtils;
import com.miui.antispam.policy.a;
import com.miui.antispam.policy.a.d;
import com.miui.antispam.policy.a.e;
import com.miui.antispam.policy.a.g;
import miui.provider.ExtraTelephony;

public class KeywordsWhiteListPolicy extends a {
    public KeywordsWhiteListPolicy(Context context, a.b bVar, d dVar, g gVar) {
        super(context, bVar, dVar, gVar);
    }

    public a.C0035a dbQuery(e eVar) {
        if (ExtraTelephony.containsKeywords(this.mContext, eVar.e, 4, eVar.f2370c)) {
            return new a.C0035a(this, true, 0);
        }
        return null;
    }

    public int getType() {
        return 4;
    }

    public a.C0035a handleData(e eVar) {
        if (!this.mJudge.b()) {
            return dbQuery(eVar);
        }
        String b2 = this.mJudge.b(eVar.e, eVar.f2370c);
        if (!TextUtils.isEmpty(b2)) {
            return new a.C0035a(true, 0, b2);
        }
        return null;
    }
}
