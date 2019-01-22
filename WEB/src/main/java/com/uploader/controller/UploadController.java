package com.uploader.controller;

import org.jboss.logging.Param;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@Controller
public class UploadController {

    //Save the uploaded file to this folder
    private static String WORKIN_DIR = System.getProperty("user.dir");
    private static String PARENT_WORKIN_DIR = new File(WORKIN_DIR).getAbsoluteFile().getParent();
    private static String UPLOADED_FOLDER = PARENT_WORKIN_DIR+ "/recordings";
    private static String RESULT_FOLDER = PARENT_WORKIN_DIR+ "/results";

    static {
        if(!new File(UPLOADED_FOLDER).exists()){
            new File(UPLOADED_FOLDER).mkdir();
        }
        if(!new File(RESULT_FOLDER).exists()){
            new File(RESULT_FOLDER).mkdir();
        }
    }

    @GetMapping("/")
    public String index() {
        return "home";
    }

    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/addFile")
    public String addFile() {
        return "addFile";
    }

    @GetMapping("/fileList")
    public String fileList(Model model) {
        File folder = new File(UPLOADED_FOLDER);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> listOfFilename = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                listOfFilename.add(listOfFiles[i].getName());
            }
        }
        model.addAttribute("listOfFile", listOfFiles);

        return "fileList";
    }


    @GetMapping("/resultList")
    public String resultList(Model model) throws IOException {
/*      String command = "python /c start python "+ PARENT_WORKIN_DIR+"/test.py";
        Process p = Runtime.getRuntime().exec(command);*/
        ProcessBuilder builder = new ProcessBuilder("python", "test.py");
        builder.directory(new File(PARENT_WORKIN_DIR));
        builder.redirectError();
        Process newProcess = builder.start();

        File folder = new File(RESULT_FOLDER);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> listOfFilename = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                listOfFilename.add(listOfFiles[i].getName());
            }
        }
        model.addAttribute("listOfResult", listOfFiles);

        return "resultList";
    }



    @RequestMapping(value="/file", method=RequestMethod.GET)
    @ResponseBody
    public File downloadFile(@RequestParam("name") String name, HttpServletResponse response) throws IOException {
        File file = new File(UPLOADED_FOLDER + name);
        String mimeType="application/octet-stream";
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + name +"\""));

        response.setContentLength((int)file.length());
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

        FileCopyUtils.copy(inputStream, response.getOutputStream());
        return new File(UPLOADED_FOLDER + name);
    }

    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public File downloadResult(@RequestParam("name") String name, HttpServletResponse response) throws IOException {
        File file = new File(RESULT_FOLDER + name);
        String mimeType="application/octet-stream";
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + name +"\""));

        response.setContentLength((int)file.length());
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

        FileCopyUtils.copy(inputStream, response.getOutputStream());
        return file;
    }


    @PostMapping("/upload") // //new annotation since 4.3
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Wybierz plik");
            return "redirect:uploadStatus";
        }

        try {

            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER +"/"+ file.getOriginalFilename());
            Files.write(path, bytes);

            redirectAttributes.addFlashAttribute("message",
                    "Plik został pomyślnie dodany '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/fileList";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }

    @GetMapping("/filesList")
    public String getFilesList() {
        File folder = new File(UPLOADED_FOLDER);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return "uploadStatus";
    }

}