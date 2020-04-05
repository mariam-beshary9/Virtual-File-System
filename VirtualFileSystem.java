package virtual.file.system;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

public class VirtualFileSystem {

    static Allocation allocation;
    static int diskSize = 10000;

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the allocation technique\n1-Contigious\n2-indexed ");
        String allocationType = sc.nextLine();

        if (allocationType.equals("1")) {
            allocation = new ContiguousAllocation(diskSize);
            // System.out.print(44);

        } else if (allocationType.equals("2")) {
            allocation = new IndexedAllocation(diskSize);

        }
        System.out.println("Do you want to upload the file system? y/n\nIf you entered n the file system will be re-initialized.");
        String ans = sc.nextLine();
        if (ans.equals("y")) {
            allocation.readData();
        }

        while (true) {

            System.out.print("Enter your command, to exit enter e: ");

            String command = sc.nextLine();

            String[] split = command.split(" ");

            if (split[0].equals("e")) {
                String data = "";
                data = allocation.storeData(data, allocation.root);

                ArrayList<String> commands = new ArrayList<>();

                String[] parts = data.split(System.getProperty("line.separator"));

                for (int i = 0; i < parts.length; i++) {
                    if (!commands.contains(parts[i]))
                    commands.add(parts[i]);
                }

                if (allocationType.equals("1")) {

                    //FileWriter fw=new FileWriter("ContigiousAllocation.txt");
                    //System.out.println("1");
                    //fw.write(data);
                    try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("ContigiousAllocation.txt"), "utf-8"))) {
                        for (int i = 0; i < commands.size(); i++) {
                            writer.write(commands.get(i));
                            writer.write(System.getProperty("line.separator"));
                        }
                    }

                } else if (allocationType.equals("2")) {

                    try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("IndexedAllocation.txt"), "utf-8"))) {
                        for (int i = 0; i < commands.size(); i++) {
                            writer.write(commands.get(i));
                            writer.write(System.getProperty("line.separator"));
                        }
                    }
                }
                break;
            } //CreateFile root/file.txt 100
            else if (split[0].equals("CreateFile")) {
                if (split.length != 3) {
                    System.out.println("CreateFile takes 2 arguments: the path and the size.");
                    continue;
                } else {

                    String[] path = split[1].split("/");
                    String p = "";
                    for (int i = 0; i < path.length - 1; i++) {
                        if (!path[i].equals("")) {
                            p += path[i];
                            p += "/";
                        }
                    }
                    // System.out.println(path[path.length-2]);

                    Directory directory = allocation.getPathDirectory(path);
                    if (directory == null) {
                        System.out.println("The directory does not exists.");
                        continue;
                    }
                    boolean noRepeat = true;
                    for (int i = 0; i < directory.files.length; i++) {
                        if (directory.files[i].name.equals(path[path.length - 1])) {
                            System.out.println("There is another file with same name in the directory.");
                            noRepeat = false;
                            break;
                        }
                    }
                    if (noRepeat) {
                        allocation.addFile(directory, p, Integer.parseInt(split[2]), path[path.length - 1]);
                    }

                }
            } //CreateFolder root/folder1
            else if (split[0].equals("CreateFolder")) {
                if (split.length != 2) {
                    System.out.println("CreateFile takes one arguments: the path.");
                    continue;
                } else {
                    String[] path = split[1].split("/");
                    String p = "";
                    for (int i = 0; i < path.length - 1; i++) {
                        if (!path[i].equals("")) {
                            p += path[i];
                            p += "/";
                        }
                    }
                    Directory directory = allocation.getPathDirectory(path);
                    if (directory == null) {
                        System.out.println("The directory does not exists.");
                        continue;
                    }
                    boolean noRepeat = true;
                    for (int i = 0; i < directory.subDirectories.length; i++) {
                        if (directory.subDirectories[i].name.equals(path[path.length - 1])) {
                            System.out.println("There is another folder with same name in the directory.");
                            noRepeat = false;
                            break;
                        }
                    }
                    if (noRepeat) {
                        allocation.addDirectory(directory, path[path.length - 1], p);
                    }
                }

            } //DeleteFile root/folder1/file.txt  
            else if (split[0].equals("DeleteFile")) {
                if (split.length != 2) {
                    System.out.println("DeleteFile takes one arguments: the path.");
                    continue;
                } else {
                    String[] path = split[1].split("/");
                    String p = "";
                    for (int i = 0; i < path.length - 1; i++) {
                        p += path[i];
                    }
                    Directory directory = allocation.getPathDirectory(path);
                    if (directory == null) {
                        System.out.println("The directory does not exists.");
                        continue;
                    }

                    int k = -1;
                    for (int i = 0; i < directory.files.length; i++) {
                        if (directory.files[i].name.equals(path[path.length - 1])) {
                            k = i;
                            break;
                        }
                    }
                    if (k == -1) {
                        System.out.println("The file does not exists.");
                        continue;
                    }
                    allocation.deleteFile(directory.files[k]);
                }
            } //DeleteFolder root/folder1
            else if (split[0].equals("DeleteFolder")) {
                if (split.length != 2) {
                    System.out.println("DeleteFolder takes one arguments: the path.");
                    continue;
                } else {
                    String[] path = split[1].split("/");
                    String p = "";
                    for (int i = 0; i < path.length - 1; i++) {
                        p += path[i];
                    }
                    Directory directory = allocation.getPathDirectory(path);
                    if (directory == null) {
                        System.out.println("The directory does not exists.");
                        continue;
                    }

                    int k = -1;

                    for (int i = 0; i < directory.subDirectories.length; i++) {
                        if (directory.subDirectories[i].name.equals(path[path.length - 1])) {

                            k = i;
                        }
                    }
                    if (k == -1) {
                        System.out.println("The directory does not exists.");
                        continue;
                    }
                    allocation.deleteDirectory(directory.subDirectories[k]);
                }
            } //DisplayDiskStatus
            else if (split[0].equals("DisplayDiskStatus")) {
                allocation.DisplayDiskStatus();
            } //DisplayDiskStructure
            else if (split[0].equals("DisplayDiskStructure")) {
                allocation.printDirectoryStructure("", allocation.root);

            }
            else {
                System.out.println("You have entered an invalid command!");
            }
        }
    }

}
