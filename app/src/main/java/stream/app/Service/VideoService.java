package stream.app.Service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import stream.app.entities.Video;

import java.util.List;

@Service
public interface VideoService {

    Video  save(Video video, MultipartFile multipartFile);

    Video  get(String id);

    Video getByTitle(String title);

    List<Video> getAll();


}
