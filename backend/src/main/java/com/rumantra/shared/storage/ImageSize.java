package com.rumantra.shared.storage;

public enum ImageSize {
  ORIGINAL(0), // Full resolution
  LARGE(1920), // 1920px width
  MEDIUM(800); // 800px width

  private final int maxWidth;

  ImageSize(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public int getMaxWidth() {
    return maxWidth;
  }
}
