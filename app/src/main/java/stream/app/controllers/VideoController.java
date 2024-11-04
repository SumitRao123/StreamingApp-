package stream.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stream.app.Constants.AppContants;
import stream.app.Service.VideoService;
import stream.app.entities.Video;
import stream.app.payload.CustomMessage;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {

    @Autowired
    VideoService videoService;

    @PostMapping
    public ResponseEntity<?>  create(
            @RequestParam("file")MultipartFile multipartFile,
            @RequestParam("title") String title,
            @RequestParam("description") String description
            )
    {
            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            video.setVideoId(UUID.randomUUID().toString());

            Video savedVideo = videoService.save(video,multipartFile);
            if(savedVideo != null){
                 return ResponseEntity.status(HttpStatus.OK).body(video);
            }
            else{
                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CustomMessage.builder().message("Video not uploaded").success(false).build());
            }

    }
    @GetMapping
    List<Video> getAll(){
        return videoService.getAll();
    }

    @Value("${files.video.hsl}")
    String HSL_DIR;


    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> getVideo(@PathVariable String videoId){
        try{
            Video video = videoService.get(videoId);
            String contentType =  video.getContentType();
            String filepath = video.getFilePath();
            Resource resource = new FileSystemResource(filepath);
            if(contentType == null){
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        }catch (Exception ex){

            System.out.println("Error occured");

        }
        return  new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,null);
    }

    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(@PathVariable String videoId,@RequestHeader(value = "Range",required = false) String range){
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        String filePath = video.getFilePath();
        if(contentType == null){
             contentType = "application/octet-stream";
        }
        Path path = Paths.get(filePath);
        Resource resource = new FileSystemResource(path);
        if(range == null){
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        }
        long fileLength  = path.toFile().length();
        long rangeStart;
        long rangeEnd;

        String arr[]  = range.replace("bytes=","").split("-");
        rangeStart = Long.parseLong(arr[0]);
        rangeEnd = rangeStart + AppContants.CHUNK_SIZE - 1;
        if(rangeEnd >= fileLength){
            rangeEnd = fileLength-1;
        }
        InputStream  inputStream;
        try{
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart  + 1;
            byte data[] = new byte[(int) contentLength];
            int read = inputStream.read(data,0,data.length);
            HttpHeaders  httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Range","bytes " + rangeStart + "-" +rangeEnd + "/" + fileLength);
            httpHeaders.add("Cache-Control","no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma","no-cache");
            httpHeaders.add("Expires","0");
            httpHeaders.add("X-Content-Type-Options","nosniff");
            httpHeaders.setContentLength(contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(httpHeaders).contentType(MediaType.parseMediaType(contentType)).body(new ByteArrayResource(data));

        }catch (Exception ex){
            System.out.println("errror");

        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/{videoId}/master.m3u8")
    public  ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId){
          Path path = Paths.get(HSL_DIR,videoId,"master.m3u8");
          if(!Files.exists(path)){
              return new ResponseEntity<>(HttpStatus.NOT_FOUND);
          }
          Resource resource =  new FileSystemResource(path);
          return  ResponseEntity
                  .ok()
                  .header(
                          HttpHeaders.CONTENT_TYPE, "application/dash+xml"
                  )
                  .body(resource);
    }
    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(
            @PathVariable String videoId,
            @PathVariable String segment
    ) {

        // create path for segment
        Path path = Paths.get(HSL_DIR, videoId, segment + ".ts");
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "video/mp2t"
                )
                .body(resource);

    }
}
