package com.zikeyang.contube.example.file;

import com.zikeyang.contube.api.Context;
import com.zikeyang.contube.api.Sink;
import com.zikeyang.contube.api.TubeRecord;
import com.zikeyang.contube.common.Utils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileSink implements Sink {
  FileTubeConfig config;
  OutputStream writer;
  Context context;

  @Override
  public void open(Map<String, Object> config, Context context) {
    log.info("FileSink open");
    this.config = Utils.loadConfig(config, FileTubeConfig.class);
    this.context = context;
    try {
      writer = new FileOutputStream(this.config.getPath());
    } catch (IOException e) {
      context.fail(e);
    }
  }

  @Override
  public void write(Collection<TubeRecord> records) {
    try {
      for (TubeRecord record : records) {
        writer.write(record.getValue());
        writer.write(System.lineSeparator().getBytes());
      }
      writer.flush();
      records.forEach(TubeRecord::commit);
    } catch (IOException e) {
      context.fail(e);
    }
  }

  @Override
  public void close() throws IOException {
    log.info("FileSink close");
    writer.close();
  }
}
