package virtual.file.system;

class File {

    String name;
    String filePath;
    int[] allocatedBlocks;
    public String indexBlockContent;
    private boolean deleted;
    public int size;

    File(String path, int size, String n) {
        name = n;
        filePath = path;
        allocatedBlocks = new int[size];
        deleted = false;
        indexBlockContent="";
        this.size = size;
    }


    public void delete() {
        deleted = true;
    }

    public void setAllocatedBlock(int i, int value) {
        allocatedBlocks[i] = value;
    }

    public int[] getAllocatedBlocks() {
        return allocatedBlocks;
    }

    public boolean isDeleted() {
        return deleted;
    }
}

public class Directory {

    String name;
    String directoryPath;
    File[] files;
    public Directory[] subDirectories;
    private boolean deleted;

    Directory(String path, String n) {
        name = n;
        directoryPath = path;
        deleted = false;
        files=new File [0];
        subDirectories = new Directory [0];
    }
        Directory( String n) {
        name = n;
        directoryPath = "";
        deleted = false;
        files=new File [0];
        subDirectories = new Directory [0];
    }


    void incrementFile() {
        int size = files.length;
        File newItems[] = new File[size + 1];
        System.arraycopy(files, 0, newItems, 0, size);
        files = newItems;
    }
    boolean isDeleted()
    {
        return deleted;
    }
    void incrementDirectory() {
        int size = subDirectories.length;
        Directory newItems[] = new Directory[size + 1];
        System.arraycopy(subDirectories, 0, newItems, 0, size);
        subDirectories = newItems;
    }

    public void addFile(File file) {
        incrementFile();
        files[files.length - 1] = file;
    }

    public void compactFiles() {
        int newSize = 0;

        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDeleted()) {
                files[newSize] = files[i];
                newSize++;
            }
        }
        File newItems[] = new File[newSize];
        System.arraycopy(files, 0, newItems, 0, newSize);
        files = newItems;
    }

    public void addDirectory(Directory d) {

        incrementDirectory();
        subDirectories[subDirectories.length - 1] = d;
    }

    public Directory[] getSubDirectories() {
        return subDirectories;
    }

    public String getName() {
        return name;
    }

    public void delete() {
        deleted = true;
    }

   
}
