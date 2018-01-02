package com.artifex.mupdfdemo;

import android.graphics.Bitmap;
import android.graphics.PointF;

public class MuPDFCore {
    static {
        System.loadLibrary("mupdf_java");
    }

    private int numPages = -1;
    private float pageWidth;
    private float pageHeight;
    private long globals;
    private byte fileBuffer[];
    private String file_format;
    private boolean isUnencryptedPDF;
    private final boolean wasOpenedFromBuffer;

    public MuPDFCore(byte buffer[], String magic) throws Exception {
        fileBuffer = buffer;
        globals = openBuffer(magic != null ? magic : "");
        if (globals == 0) {
            throw new Exception("Can not open buffer");
        }
        file_format = fileFormatInternal();
        isUnencryptedPDF = isUnencryptedPDFInternal();
        wasOpenedFromBuffer = true;
    }

    public MuPDFCore(String filename) throws Exception {
        globals = openFile(filename);
        if (globals == 0) {
            throw new Exception("Can not open file " + filename);
        }
        file_format = fileFormatInternal();
        isUnencryptedPDF = isUnencryptedPDFInternal();
        wasOpenedFromBuffer = false;
    }

    public class Cookie {
        private final long cookiePtr;

        public Cookie() {
            cookiePtr = createCookie();
            if (cookiePtr == 0) {
                throw new OutOfMemoryError();
            }
        }

        public void abort() {
            abortCookie(cookiePtr);
        }

        public void destroy() {
            // We could do this in finalize, but there's no guarantee that
            // a finalize will occur before the muPDF context occurs.
            destroyCookie(cookiePtr);
        }
    }

    public synchronized void drawPage(Bitmap b, int page, int w, int h, int patchX, int patchY, int patchW, int patchH, Cookie c) {
        gotoPage(page);
        drawPage(b, w, h, patchX, patchY, patchW, patchH, c.cookiePtr);
    }

    public synchronized boolean authenticatePassword(String password) {
        return authenticatePasswordInternal(password);
    }

    public synchronized boolean needsPassword() {
        return needsPasswordInternal();
    }

    public synchronized PointF getPageSize(int currentPageIndex) {
        gotoPage(currentPageIndex);
        return new PointF(pageWidth, pageHeight);
    }

    private void gotoPage(int page) {
        if (page > numPages - 1) {
            page = numPages - 1;
        } else if (page < 0) {
            page = 0;
        }
        gotoPageInternal(page);
        this.pageWidth = getPageWidth();
        this.pageHeight = getPageHeight();
    }

    private synchronized int countPagesSynchronized() {
        return countPagesInternal();
    }

    public int countPages() {
        if (numPages < 0) {
            numPages = countPagesSynchronized();
        }
        return numPages;
    }

    /* The native functions */
    private native long openFile(String filename);

    private native long openBuffer(String magic);

    private native String fileFormatInternal();

    private native boolean isUnencryptedPDFInternal();

    private native void gotoPageInternal(int localActionPageNum);

    private native float getPageHeight();

    private native float getPageWidth();

    private native int countPagesInternal();

    private native void drawPage(Bitmap bitmap, int pageW, int pageH, int patchX, int patchY, int patchW, int patchH, long cookiePtr);

    private native boolean authenticatePasswordInternal(String password);

    private native boolean needsPasswordInternal();

    private native void destroyCookie(long cookieToDestroy);

    private native void abortCookie(long cookie);

    private native long createCookie();
}
