package com.zikeyang.contube.runtime;

import com.zikeyang.contube.api.Con;
import com.zikeyang.contube.api.Source;
import com.zikeyang.contube.api.TombstoneRecord;
import com.zikeyang.contube.api.TubeRecord;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SourceTube extends Tube {
  final Con con;
  Source source;

  public SourceTube(TubeConfig config, Con con) {
    super(config);
    this.con = con;
  }

  void init() throws Exception {
    source = createTube(config.getClazz(), Source.class);
    source.open(config.getConfig(), createContext());
  }

  @SneakyThrows
  void runTube() {
    TubeRecord record = source.read();
    con.send(config.getSinkTubeName(), record);
    if (record instanceof TombstoneRecord) {
      log.trace("Got tombstone record");
      if (!closed.compareAndSet(false, true)) {
        deathException = new IllegalStateException("Tube already closed");
        return;
      }
      throw new InterruptedException();
    }
  }

  @Override
  public void close() {
    try {
      if (source != null) {
        source.close();
      }
    } catch (Exception e) {
      log.error("Closing tube failed", e);
    }
  }
}