package com.jrmf.api.task;

import com.jrmf.utils.PdUtil;

public class MergePdfTask implements Runnable {
  private String[] files;
  private String newFile;

  public MergePdfTask() {
  }

  public MergePdfTask(String[] files, String newFile) {
    this.files = files;
    this.newFile = newFile;
  }

  @Override
  public void run() {
    PdUtil.mergePdfFiles(files,newFile);
  }
}
