package com.zikeyang.contube.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Runtime {

  public static void main(String[] args) {
    if (args.length < 1 || Arrays.asList(args).contains("--help")) {
      log.info("runtime [tube1.yaml tube2.yaml ...]");
      System.exit(1);
    }

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    List<Thread> tubes = new ArrayList<>();
    MemoryCon memoryCon = new MemoryCon();

    for (String arg : args) {
      try {
        TubeConfig config = mapper.readValue(new File(arg), TubeConfig.class);
        log.info("Starting tube with config: {}", config);
        switch (config.getType()) {
          case SOURCE -> {
            SourceTube sourceTube = new SourceTube(config, memoryCon);
            Thread thread = new Thread(sourceTube);
            tubes.add(thread);
            thread.start();
          }
          case SINK -> {
            SinkTube sinkTube = new SinkTube(config);
            memoryCon.addSinkTube(config.getName(), sinkTube);
            Thread thread = new Thread(sinkTube);
            tubes.add(thread);
            thread.start();
          }
        }

      } catch (Exception e) {
        log.error("Staring tube failed", e);
        System.exit(1);
      }
    }
    for (Thread tube : tubes) {
      try {
        tube.join();
      } catch (InterruptedException e) {
        log.error("Waiting for tube to stop failed", e);
        System.exit(1);
      }
    }
  }
}