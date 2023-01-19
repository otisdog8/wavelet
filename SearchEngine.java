import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.locks.*;;

class SearchHandler implements URLHandler {
    // The one bit of state on the server: a number that will be manipulated by
    // various requests.
    HashSet<String> strs = new HashSet<>();
    HashMap<String, HashSet<String>> substrs = new HashMap<>();
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private HashSet<String> genSubStr(String s) {
        HashSet<String> result = new HashSet<>();
        for (int i = 0; i < s.length(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < s.length(); j++) {
                sb.append(s.charAt(j));
                result.add(sb.toString());
            }
        }
        return result;
    }

    public String handleRequest(URI url) {
        if (url == null) {
            return "Error";
        }
        String quString = url.getQuery();
        System.out.println(quString);
        if (quString == null) {
            return "Error";
        }
        System.out.println("aa");
        String[] quArr = quString.split("=");
        System.out.println("ab");

        String s = null;
        System.out.println("ac");

        for (int i = 0; i < quArr.length-1; i += 2) {
            if (quArr[i].equals("s")) {
                s = quArr[i+1];
            }
        }
        System.out.println("ad");

        if (s == null) {
            return "Invalid querystring";
        }
        if (url.getPath().endsWith("/add")) {
            Lock wl = lock.writeLock();
            try {
                wl.lock();
                if (strs.contains(s)) {
                    return "String already in searchdb";
                }
                for (String str : genSubStr(s)) {
                    if (!substrs.containsKey(str)) {
                        substrs.put(str, new HashSet<>());
                    }
                    substrs.get(str).add(s);
                }
                strs.add(s);
                return "String added";
            }
            finally {
                wl.unlock();
            }

        }
        else if (url.getPath().endsWith("/remove")) {
            Lock wl = lock.writeLock();
            try {
                wl.lock();
                if (!strs.contains(s)) {
                    return "String not in searchdb";
                }
                for (String str : genSubStr(s)) {
                    substrs.get(str).remove(s);
                }
                strs.remove(s);
                return "String removed";
            }
            finally {
                wl.unlock();
            }

        }
        else if (url.getPath().endsWith("/search")) {
            Lock rl = lock.readLock();
            try {
                rl.lock();
                if (s.equals("")) {
                    return strs.toString();
                }
                else {
                    if (substrs.containsKey(s)) {
                        return substrs.get(s).toString();
                    }
                    else {
                        return "No strings found";
                    }
                }
            }
            finally {
                rl.unlock();
            }

        }
        else {
            return "404";
        }
    }
}

class SearchEngine {
    public static void main(String[] args) throws IOException {
        if(args.length == 0){
            System.out.println("Missing port number! Try any number between 1024 to 49151");
            return;
        }

        int port = Integer.parseInt(args[0]);

        Server.start(port, new SearchHandler());
    }
}