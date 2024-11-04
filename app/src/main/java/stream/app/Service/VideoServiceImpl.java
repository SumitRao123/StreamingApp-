package stream.app.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import stream.app.entities.Video;
import stream.app.repository.VideoRepository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoServiceImpl implements VideoService{

     @Value("${files.video}")
     String DIR;

     @Value("${files.video.hsl}")
     String HSL_DIR;

     @Autowired
    VideoRepository videoRepository;

     @PostConstruct
     public void init(){
       File file = new File(DIR);
       try{
           Files.createDirectories(Paths.get(HSL_DIR));
       }catch (Exception e){
           System.out.println("Errror in init");
       }


       if(!file.exists()){
           file.mkdir();
           System.out.println("Folder created ");
       }
       else{
           System.out.println("Folder is already existing ");
       }
     }


    @Override
    public Video save(Video video, MultipartFile multipartFile) {
          try{
              String filename= multipartFile.getOriginalFilename();
              String contentType = multipartFile.getContentType();
              InputStream inputStream = multipartFile.getInputStream();


              // Folder Path : create
              String cleanFileName = StringUtils.cleanPath(filename);
              String cleanFolder = StringUtils.cleanPath(DIR);
              // folder path with filename;
              Path path = Paths.get(cleanFolder,cleanFileName);
              System.out.println(path);
              // copy file to folder
              Files.copy(inputStream,path, StandardCopyOption.REPLACE_EXISTING);

              video.setFilePath(path.toString());
              video.setContentType(contentType);

              Video SavedVideo = videoRepository.save(video);

              processVideo(SavedVideo.getVideoId());

              return SavedVideo;

          }catch (Exception e){
               e.printStackTrace();

          }
          return null;
    }

    @Override
    public Video get(String id) {
        return videoRepository.findById(id).orElseThrow(()->new RuntimeException("Video is not avaliable"));
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    void  processVideo(String VideoId) {
            Video video = this.get(VideoId);
            String filePath = video.getFilePath();
            Path videoPath =  Paths.get(filePath);

            try{
              Path outputPath = Paths.get(HSL_DIR,VideoId);
                Files.createDirectories(outputPath);
                String ffmpegCmd = String.format(
                        "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s\\segment_%%3d.ts\" \"%s\\master.m3u8\"",
                        videoPath, outputPath, outputPath
                );

                // Print the ffmpeg command for verification
                System.out.println("Executing command: " + ffmpegCmd);

                // ProcessBuilder to execute the command in Windows CMD
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);

                // Make sure the command output is inherited and printed to the console
                processBuilder.inheritIO();

                // Start the process
                Process process = processBuilder.start();

                // Wait for the process to complete and get the exit code
                int exitCode = process.waitFor();

                // If the exit code is not 0, throw an error indicating failure
                if (exitCode != 0) {
                    throw new RuntimeException("Video processing failed! Exit code: " + exitCode);
                } else {
                    System.out.println("Video processing completed successfully.");
                }



            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }

    }
}
