package virtual.file.system;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class ContiguousAllocation extends Allocation {

    public ContiguousAllocation(int n) {
        super(n);
    }

    @Override
    public int checkFreeSpace(int size) {
        int s = 1;
        int temp = 0;
        for (int i = 0; i < diskBlocks.length; i++) {
            if (diskBlocks[i] == false) {
                s++;
            } else if (diskBlocks[i] == true) {
                // set it =1 because we want the space contigious
                s = 1;
                temp = i + 1;  //the block after the allocated one
            }
            if (s == size) {
                return temp;
            }
        }

        return -1;
    }

    @Override
    public boolean addFile(Directory directory, String path, int size, String n) {
        int index = checkFreeSpace(size);

        if (index == -1) {
            return false;
        } else {
            freeSize -= size;  // to update the free size of the disk.
            File file = new File(path, size, n);
            directory.addFile(file);
            for (int i = 0; i < size; i++) {
                diskBlocks[index] = true;
                file.allocatedBlocks[i]=index; 
                index++;
            }
            return true;
        }
    }

    @Override
    public String storeData(String data, Directory d) {
        if (!d.isDeleted()) {
            if (!d.name.equals("root")) {
                data += "CreateFolder " + d.directoryPath + d.name + System.getProperty("line.separator");
            } else {
                data += "CreateFolder " + d.name + System.getProperty("line.separator");
            }
        }

        for (int i = 0; i < d.files.length; i++) {
            if (!d.files[i].isDeleted()) {
                data += "CreateFile " + d.files[i].filePath + d.files[i].name + " " + d.files[i].size + " " + d.files[i].allocatedBlocks[0] + " " + d.files[i].allocatedBlocks[d.files[i].allocatedBlocks.length - 1] + System.getProperty("line.separator");
            }
        }
        if (d.subDirectories != null) {
            for (int i = 0; i < d.subDirectories.length; i++) {
                data += storeData(data, d.subDirectories[i]);
            }
        }

        return data;
    }

    @Override
    public void readData() {

        
        BufferedReader reader = null;
        try {
            ArrayList <String> commands = new ArrayList <>();
            FileInputStream ifile = new FileInputStream("ContigiousAllocation.txt");

            Scanner sc = new Scanner(ifile);

            while (sc.hasNextLine()) {
                String command = sc.nextLine();

                String[] split = command.split(" ");

                //CreateFile root/file.txt 100
                if (split[0].equals("CreateFile")) {

                    String[] path = split[1].split("/");
                    String p = "";
                    for (int i = 0; i < path.length - 1; i++) {
                        p += path[i];
                    }
                    //  System.out.println(path[path.length-2]);
                    Directory directory = this.getPathDirectory(path);
                    if (directory == null) {

                        continue;
                    }
                    boolean noRepeat = true;
                    for (int i = 0; i < directory.files.length; i++) {
                        if (directory.files[i].name.equals(path[path.length - 1])) {
                            System.out.println("There is another file with same name in the directory.");
                            noRepeat = false;
                            continue;
                        }
                    }
                    if (noRepeat) {
                        this.freeSize -= Integer.parseInt(split[2]);
                        for (int i = 0; i < path.length - 1; i++) {
                            p += path[i];
                            p += "/";
                        }
                        File file = new File(p, Integer.parseInt(split[2]), path[path.length - 1]);
                        
                        file.allocatedBlocks = new int [Integer.parseInt(split[2])];
                        int j = 0;
                        
                        for (int i = Integer.parseInt(split[3]); i <= Integer.parseInt(split[4]); i++) {
                            this.diskBlocks[i] = false;
                            file.allocatedBlocks[j] = i;
                            j++;
                        }
                        directory.addFile(file);
                    }
                } //CreateFolder root/folder1
                else if (split[0].equals("CreateFolder")) {

                    String[] path = split[1].split("/");
                    String p = "";
                    for (int i = 0; i < path.length - 1; i++) {
                        p += path[i];
                        p += "/";
                    }
                    Directory directory = this.getPathDirectory(path);
                    if (directory == null) {
                        System.out.println("The directory does not exists.");
                        continue;
                    }
                    boolean noRepeat = true;
                    for (int i = 0; i < directory.subDirectories.length; i++) {
                        if (directory.subDirectories[i].name.equals(path[path.length - 1])) {
                            System.out.println("There is another folder with same name in the directory.");
                            noRepeat = false;
                            continue;
                        }
                    }
                    if (noRepeat) {
                        this.addDirectory(directory, path[path.length - 1],p);

                    }
                }
                // read next line
                //command = sc.nextLine();
            }
           
        } catch (FileNotFoundException ex) {
            System.out.println("read nothing from files");
        }
    }

@Override
    public void deleteFile(File file) {
        int[] blocks = file.getAllocatedBlocks();
        for (int i = 0; i < blocks.length; i++) {
            diskBlocks[blocks[i]] = false;
        }
        freeSize += file.size;
        file.delete();
    }
}

class IndexedAllocation extends Allocation {

    public IndexedAllocation(int n) {
        super(n);
    }

    @Override
    public boolean addFile(Directory directory, String path, int size, String n) {
        int index = checkFreeSpace(size+1);
        if (index == -1) {
            return false;
        } else {
            freeSize -= size;
            File file = new File(path, size, n);
            directory.addFile(file);
            int j = 0;
            int indexBlock = getIndexBlock();
            file.setAllocatedBlock(j, indexBlock); // when the allocation is indexed I put only the index block
            for (int i = index; i < diskBlocks.length; i++) {
                if (diskBlocks[i] == false) {
                    diskBlocks[i] = true;
                    file.indexBlockContent += i + " "; // setting the blocks that the index block contains
                    //  file.setAllocatedBlock(j, i);
                    j++;

                    if (j == size) {
                        break;
                    }

                }
            }
            return true;
        }
    }

    @Override
    public int checkFreeSpace(int size) {
        int s = 1;
        int n = -1; // to get first empty
        for (int i = 0; i < diskBlocks.length; i++) {
            if (diskBlocks[i] == false) {
                s++;
                if (n == -1) { // get the first empty one
                    n = i;
                }
            }
            if (s == size) {
                return n;
            }
        }
        return -1;
    }

    public int getIndexBlock() {
        int n = 0;
        for (int i = 0; i < diskBlocks.length; i++) {
            if (diskBlocks[i] == false) {
                return i;

            }
        }
        return -1;
    }
   
    @Override
    public String storeData(String data, Directory d) {

        if (!d.isDeleted()) {
            if(!d.name.equals("root"))
            data += "CreateFolder " + d.directoryPath + d.name + System.getProperty("line.separator");
            else
             data += "CreateFolder " + d.name + System.getProperty("line.separator");
        }

        for (int i = 0; i < d.files.length; i++) {
            if (!d.files[i].isDeleted()) {
              data += "CreateFile " + d.files[i].filePath +d.files[i].name+" " + d.files[i].size+" " + d.files[i].allocatedBlocks[0] + " " + d.files[i].indexBlockContent + System.getProperty("line.separator");
            }
        }
        if (d.subDirectories != null) {
            for (int i = 0; i < d.subDirectories.length; i++) {
                data+=storeData(data, d.subDirectories[i]);
            }
        }
        return data;
    }

    @Override
    public void readData() {
        try {
            ArrayList <String> commands = new ArrayList <>();
            FileInputStream ifile = new FileInputStream("IndexedAllocation.txt");

            Scanner sc = new Scanner(ifile);

            while (sc.hasNextLine()) {
                String command = sc.nextLine();
            
                
                String[] split = command.split(" ");
                //CreateFile root/file.txt 100
                if (split[0].equals("CreateFile")) {

                    String[] path = split[1].split("/");
                    String p = "";
                    for (int i = 0; i < path.length - 1; i++) {
                        p += path[i];
                    }
                    //  System.out.println(path[path.length-2]);
                    Directory directory = this.getPathDirectory(path);

                    this.freeSize -= Integer.parseInt(split[2]);
                    for (int i = 0; i < path.length - 1; i++) {
                        p += path[i];
                        p+="/";
                    }
                    File file = new File(p, Integer.parseInt(split[2]), path[path.length - 1]);

                    this.diskBlocks[Integer.parseInt(split[3])] = false;
                    file.allocatedBlocks[0] = Integer.parseInt(split[3]);

                    for (int i = 4; i < split.length; i++) {
                        file.indexBlockContent += split[i];

                    }
                    directory.addFile(file);
                } //CreateFolder root/folder1
                else if (split[0].equals("CreateFolder")) {

                    String[] path = split[1].split("/");
                    String p = "";
                    for (int i = 0; i < path.length - 1; i++) {
                        p += path[i];
                        p+="/";
                    }
                    Directory directory = this.getPathDirectory(path);
                    this.addDirectory(directory, path[path.length - 1],p);

                }

            }
        } catch (FileNotFoundException ex) {
            System.out.println("read nothing from files");
        }
    }
 @Override
    public void deleteFile(File file) {
        String[] blocks = file.indexBlockContent.split(" ");
        for (int i = 0; i < blocks.length; i++) {
            diskBlocks[Integer.parseInt(blocks[i])] = false;
        }
        diskBlocks[file.allocatedBlocks[0]] = false;
        freeSize += file.size+1;
        file.delete();
    }
}

public abstract class Allocation {

    boolean[] diskBlocks;
    Directory root;
    int freeSize;
    int diskSize;

    public Allocation(int n) {
        diskBlocks = new boolean[n];
        root = new Directory("root");
        freeSize = n;
        diskSize = n;
    }

    abstract public boolean addFile(Directory directory, String path, int size, String n);

    abstract public void deleteFile(File file);

    public Directory getPathDirectory(String[] path) {

        Directory temp = this.root;
        //System.out.println("m1");
        int n = 1;
        if(path.length>1){
        while (!temp.name.equals(path[path.length - 2])) {
            // System.out.println("m2");
            for (int i = 0; i < temp.subDirectories.length; i++) {
                if (temp.subDirectories[i].name.equals(path[n])) {
                    temp = temp.subDirectories[i];
                    n++;
                    break;
                }
                if (i == temp.subDirectories.length - 1)// means we will never find it because we did not find the directory
                {
                    return null;
                }
            }

        }}
        // System.out.println("m3");
        return temp;
    }

    public boolean addDirectory(Directory directory, String name, String path) {

        Directory d = new Directory(path, name);
        directory.addDirectory(d);
        return true;
    }

    public void deleteDirectory(Directory d) {
        d.delete();
    }

    public void printDirectoryStructure(String s, Directory d) {

        if (!d.isDeleted()) {
            System.out.println(s + d.name);
        }
        s += "  ";
        for (int i = 0; i < d.files.length; i++) {
            if (!d.files[i].isDeleted()) {
                System.out.println(s + d.files[i].name);
            }
        }
        if (d.subDirectories != null) {
            for (int i = 0; i < d.subDirectories.length; i++) {
                printDirectoryStructure(s, d.subDirectories[i]);
            }
        }
    }

    public void DisplayDiskStatus() {
        System.out.println("The free space = " + freeSize);
        System.out.println("The allocated space = " + (diskSize - freeSize));

        String freeBlocks = "";
        String allocatedBlocks = "";
        for (int i = 0; i < diskBlocks.length; i++) {
            if (diskBlocks[i] == false) {
                freeBlocks += " " + i;
            } else {
                allocatedBlocks += " " + i;
            }
        }
        System.out.println("The free blocks are:");
        System.out.println(freeBlocks);
        System.out.println("The allocated blocks are:");
        System.out.println(allocatedBlocks);
    }

    abstract public int checkFreeSpace(int size);

    abstract public String storeData(String s, Directory d);

    abstract public void readData();
}
