package androidx.appcompat.app;

class B {

    /* renamed from: a  reason: collision with root package name */
    private static B f269a;

    /* renamed from: b  reason: collision with root package name */
    public long f270b;

    /* renamed from: c  reason: collision with root package name */
    public long f271c;

    /* renamed from: d  reason: collision with root package name */
    public int f272d;

    B() {
    }

    static B a() {
        if (f269a == null) {
            f269a = new B();
        }
        return f269a;
    }

    public void a(long j, double d2, double d3) {
        float f = ((float) (j - 946728000000L)) / 8.64E7f;
        float f2 = (0.01720197f * f) + 6.24006f;
        double d4 = (double) f2;
        double sin = (Math.sin(d4) * 0.03341960161924362d) + d4 + (Math.sin((double) (2.0f * f2)) * 3.4906598739326E-4d) + (Math.sin((double) (f2 * 3.0f)) * 5.236000106378924E-6d) + 1.796593063d + 3.141592653589793d;
        double d5 = (-d3) / 360.0d;
        double round = ((double) (((float) Math.round(((double) (f - 9.0E-4f)) - d5)) + 9.0E-4f)) + d5 + (Math.sin(d4) * 0.0053d) + (Math.sin(2.0d * sin) * -0.0069d);
        double asin = Math.asin(Math.sin(sin) * Math.sin(0.4092797040939331d));
        double d6 = 0.01745329238474369d * d2;
        double sin2 = (Math.sin(-0.10471975803375244d) - (Math.sin(d6) * Math.sin(asin))) / (Math.cos(d6) * Math.cos(asin));
        if (sin2 >= 1.0d) {
            this.f272d = 1;
        } else if (sin2 <= -1.0d) {
            this.f272d = 0;
        } else {
            double acos = (double) ((float) (Math.acos(sin2) / 6.283185307179586d));
            this.f270b = Math.round((round + acos) * 8.64E7d) + 946728000000L;
            this.f271c = Math.round((round - acos) * 8.64E7d) + 946728000000L;
            if (this.f271c >= j || this.f270b <= j) {
                this.f272d = 1;
                return;
            } else {
                this.f272d = 0;
                return;
            }
        }
        this.f270b = -1;
        this.f271c = -1;
    }
}
