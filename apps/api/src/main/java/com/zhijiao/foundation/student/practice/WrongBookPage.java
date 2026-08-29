package com.zhijiao.foundation.student.practice;

import java.util.List;

public record WrongBookPage(List<WrongBookItem> items, int page, int size, long total) {
}
