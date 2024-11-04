import React, { useState } from "react";
import {
  Button,
  Card,
  Label,
  TextInput,
  Textarea,
  Progress,
  Alert,
} from "flowbite-react";
import axios from "axios";
import toast from "react-hot-toast";
function VideoUpload() {
  const [SelectedFile, setSelectedFile] = useState(null);
  const [Uploading, setUploading] = useState(false);
  const [meta, setMeta] = useState({ title: "", description: "" });
  const [progress, setProgress] = useState(0);
  const [message, setMessage] = useState("");
  const handleFile = (event) => {
    setSelectedFile(event.target.files[0]);
  };
  const resetForm = ()=>{
      setMeta({title : "",description : ""});
      setProgress(0);
      setUploading(false);
      setSelectedFile(null);
  }
  const handleText = (event) => {
    setMeta({ ...meta, [event.target.name]: [event.target.value] });
  };
  const handleSubmit = (event) => {
    event.preventDefault();
    console.log(meta);
    console.log(SelectedFile);
    saveVideoToServer(SelectedFile, meta);
  };
  const saveVideoToServer = async (video, videoMetaData) => {
    try {
      
      setUploading(true);
      let formData = new FormData();
      formData.append("title", videoMetaData.title);
      formData.append("description", videoMetaData.description);
      formData.append("file", video);
      const response = await axios.post(
        "http://localhost:9090/api/v1/videos",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
          onUploadProgress: (progressEvent) => {
            const { loaded, total } = progressEvent;
            let percent = Math.floor((loaded * 100) / total);
            setProgress(percent);
          },
        }
      );
      console.log(response);
      setMessage("File uploaded successfully" + response.data.videoId);
      setUploading(false);
      toast.success("File Uploaded Succesfully");
      resetForm();
    } catch (error) {
      setUploading(false);

      console.error(error);
      setMessage("File upload failed");
      toast.error("File Not Uploaded successfully");
    }
  };
  return (
    <div className="text-white">
      <Card className="flex flex-col items-center justify-center ">
        <h1>Upload Videos</h1>
        <form className="space-y-6" onSubmit={handleSubmit}>
          <div>
            <div className="mb-2 block">
              <Label htmlFor="file-upload" value="Video Title" />
            </div>
            <TextInput
              name="title"
              placeholder="Enter title"
              onChange={handleText}
            />
          </div>

          <div>
            <div className="mb-2 block">
              <Label htmlFor="file-upload" value="Video Description" />
            </div>
            <Textarea
              name="description"
              placeholder="Enter Description"
              onChange={handleText}
            />
          </div>

          <div className="flex  items-center space-x-5 justify-center">
            <div className="shrink-0">
              <img
                className="h-16 w-16 object-cover"
                src="../src/assets/video-posting.png"
                alt="Current profile photo"
              />
            </div>
            <label className="block">
              <span className="sr-only">Choose Video File</span>
              <input
                type="file"
                className="block w-full text-sm text-slate-500
                    file:mr-4 file:py-2 file:px-4
                    file:rounded-full file:border-0
                    file:text-sm file:font-semibold
                    file:bg-violet-50 file:text-violet-700
                    hover:file:bg-violet-100
                    "
                onChange={handleFile}
              />
            </label>
          </div>
          <div>
            {Uploading && (
              <Progress
                hidden={!Uploading}
                progress={progress}
                textLabel="Uploading"
                size="xl"
                labelProgress
                labelText
                className=" text-center "
              />
            )}
          </div>
          <div>
            {message && (
              <Alert color={"success"} rounded withBorderAccent  onDismiss={() => {
                    setMessage("");
                  }} >
                <span className=" font-medium">Success </span>
                {message}
              </Alert>
            )}
          </div>
          <div className=" flex  justify-center">
            <Button disabled={Uploading} type="submit">
              Upload
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}

export default VideoUpload;
