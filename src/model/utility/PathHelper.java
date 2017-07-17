/**
  * This PathHelper class provides full-path retrieving from page and check the path is valid.
  * @author yu-shuan
  */
package model.utility;
import system.SystemSettings;
import system.data.SetPage;
import system.data.Settings;
import system.data.SimplePage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PathHelper {
  /*
   * @see SystemSettings 
   */
  private static final String[] defaultDirectories = {
      SystemSettings.D_draft, SystemSettings.D_draft + "/" + SystemSettings.D_sub_page + "/", 
      SystemSettings.D_edit, SystemSettings.D_upload,
      SystemSettings.D_web, SystemSettings.D_web + "/" + SystemSettings.D_sub_page + "/",
      SystemSettings.D_css,
      SystemSettings.D_layout,SystemSettings.D_layout + "/" + SystemSettings.D_layout_xml + "/",
      SystemSettings.D_layout + "/" + SystemSettings.D_layout_preview + "/"
      };
  
  public String createDefaultDirectoy(){
    String homeDirectory = System.getProperty("user.home");
    String rootDirectoryPath = homeDirectory + SystemSettings.D_root;
    File rootDirectory = new File(rootDirectoryPath);
    
    try{
      
      if(!rootDirectory.exists()) {
        rootDirectory.mkdirs();
      }
      
      for(String subDefaultDirectory : defaultDirectories) {
        File sub = new File(homeDirectory + SystemSettings.D_root + subDefaultDirectory);
        if(!sub.exists()) {
          sub.mkdirs();
        }
      } 
      
      copyInnerDirdctoryToLocalPath(rootDirectoryPath, SystemSettings.D_css + "/");
      copyInnerDirdctoryToLocalPath(rootDirectoryPath, SystemSettings.D_edit + "/");
      copyInnerFileToLocalPath(rootDirectoryPath, "", SystemSettings.configXMLFile);
      
    }catch(Exception e) {
      e.printStackTrace();
      return null;
    }
    return rootDirectoryPath;
  }
  
  /* !!Notice: ignore the default folder.
   * @param directoryPath representing to a specific parent folder. such as web/
   */
  public String getPathFromSimplePage(SimplePage simplePage,Settings settings, String directoryPath) {
    Deque<SetPage> stack = new LinkedList<SetPage>();
    SetPage setPage = simplePage.getParent();
    
    while(setPage != null) {
      if(directoryPath.equals(settings.getLocalPath() + SystemSettings.D_edit + "/") || !setPage.getName().equals("default"))
      stack.push(setPage);
      setPage = setPage.getParent();
    }
    
    while(!stack.isEmpty()) {
      directoryPath = directoryPath + stack.pop().getName() + "/";
    }
    
    return directoryPath + simplePage.getName();
  }
  
  public boolean checkPathFromSimplePage(SimplePage simplePage, int type) {
    
    String pathUrl = "";
    
    if(type == 1){
      pathUrl = SystemSettings.D_web + "/";
    }
    else {
      pathUrl = SystemSettings.D_draft + "/";
    }
    
    Deque<SetPage> stack = new LinkedList<SetPage>();
    SetPage setPage = simplePage.getParent();
    
    while(setPage != null && !setPage.getName().equals(SystemSettings.D_draft)) {
      stack.push(setPage);
      setPage = setPage.getParent();
    }
    
    while(!stack.isEmpty()) {
      pathUrl = pathUrl + stack.pop().getName() + "/";
      
      try{
        if(!Files.exists(Paths.get(pathUrl))){
          Files.createDirectories(Paths.get(pathUrl));
        }
      } catch(Exception e) {
        e.printStackTrace();
        return false;
      }
    }
    
   return true; 
  }
  
  public void deleteFilesFromDirectory(String directoryPath) {
    File rootDirectoy = new File(directoryPath);
    List<File> currentList;
    List<File> tempList = new LinkedList<File>();
    Deque<File> stack = new ArrayDeque<File>();
    
    if(rootDirectoy.exists()) {
      currentList = Arrays.asList(rootDirectoy.listFiles());
      stack.push(rootDirectoy);
      
      while(true) {
        for(File existingFile : currentList) {
          if(existingFile.isDirectory()) {
            stack.push(existingFile);
            tempList.addAll(Arrays.asList(existingFile.listFiles()));
          }
        }
        if(tempList.isEmpty()){
          break;
        }
        currentList = tempList.stream().collect(Collectors.toList());
        tempList.clear();
      }
      
      while(!stack.isEmpty()){
        for(File file : stack.pop().listFiles()) {
          if(!file.isDirectory()) {
            file.delete();
          }
        }
      }
    }
    else {
      System.out.println("root directory error");
    }
  }
  
  private static void copyInnerDirdctoryToLocalPath(String localPath, String innerPath) {
    List<File> fileList;
    List<File> temp = new ArrayList<File>();
    File innerDirectory = new File(innerPath);

    fileList = Arrays.asList(innerDirectory.listFiles());
    
    while(true){
      for(File file : fileList) {
        File newFile = new File(localPath + file.getPath());
        if(file.isDirectory()) {
          if(!newFile.exists()) {
            newFile.mkdirs();
          }
          temp.addAll(Arrays.asList(file.listFiles()));
        }
        else {
          if(!newFile.exists()) {
            try{
              Files.copy(file.toPath(), newFile.toPath());
            }
            catch(Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
      if(temp.isEmpty()) {
        break;
      }
      
      fileList = temp.stream().collect(Collectors.toList());
      temp.clear();
    }
  }
  
  private static void copyInnerFileToLocalPath(String localPath, String innerPath, String filName){
    File innerFile = new File(innerPath + filName);
    File localFile = new File(localPath + filName);
    
    if(!localFile.exists()){
      try{
        Files.copy(innerFile.toPath(), localFile.toPath());
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  
}
