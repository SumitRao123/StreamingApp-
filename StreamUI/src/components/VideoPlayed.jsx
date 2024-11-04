import React, { useEffect, useRef } from 'react'
import videojs  from "video.js"
import hls from "hls.js"

import "video.js/dist/video-js.css";
import toast from "react-hot-toast";


function VideoPlayed({src}) {
    const videoRef = useRef(null);
    const playRef = useRef(null);
    useEffect(()=>{
        playRef.current = videojs(videoRef.current,{
            controls : true,
            autoplay : true,
            muted : true,
            preload : "auto"
        })
  
        if(hls.isSupported()){
             const HLS = new hls();
             HLS.loadSource(src);
             HLS.attachMedia(videoRef.current);
             HLS.on(hls.Events.MANIFEST_PARSED,()=>{
                videoRef.current.play;
             })
        }
        else{
            console.log("Format not supported");
        }
    }
     
    
    ,[src])
  return (
    <div>
        <div data-vjs-player>
          <video 
           ref={videoRef}
           style={{
              width:"100%",
              height  : "500px"
           }}
            className=' video-js vjs-control-bar'
          >

          </video>
        </div>
    </div>
  )
}

export default VideoPlayed