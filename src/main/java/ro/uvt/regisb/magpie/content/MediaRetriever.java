package ro.uvt.regisb.magpie.content;

import java.util.List;

public interface MediaRetriever {
    boolean connect(String address);

    List<String> download(int total, Object filter);

    void disconnect();
}
