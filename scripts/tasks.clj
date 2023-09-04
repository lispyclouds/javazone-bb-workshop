(ns tasks
  (:require
   [babashka.cli :as cli]
   [babashka.fs :as fs]
   [babashka.http-client :as http]
   [babashka.process :as proc]
   [clojure.java.io :as io]))

(def lambda-file "deploy/lambda.zip")

(def layer-file "deploy/runtime_layer.zip")

(def default-arch "aarch64")

(defn latest-version
  []
  (:body (http/get "https://raw.githubusercontent.com/babashka/babashka/master/resources/BABASHKA_RELEASED_VERSION")))

(defn download
  [arch version]
  (let [arch (or arch default-arch)
        version (or version (latest-version))
        filename (format "babashka-%s-linux-%s-static.tar.gz" version arch)
        url (format "https://github.com/babashka/babashka/releases/download/v%s/%s" version filename)
        archive "target/bb.tar.gz"]
    (println "Downloading bb from:" url)
    (io/copy (:body (http/get url {:as :stream})) (io/file archive))
    (proc/shell "tar" "xvzf" archive "-C" "target")
    (fs/delete archive)))

(defn pack
  [args]
  (let [{:keys [arch version]} (cli/parse-opts args)]
    (fs/create-dirs "target")
    (download arch version)
    (fs/copy "resources/bootstrap" "target/bootstrap" {:replace-existing true})
    (fs/zip layer-file "target" {:root "target"})
    (fs/delete-tree "target")
    (fs/create-dirs "target")
    (proc/shell "bb uberscript target/function.clj -m lambda.main")
    (fs/zip lambda-file "target" {:root "target"})))

(defn deploy
  [args]
  (let [{:keys [arch]} (cli/parse-opts args)
        arch (or arch default-arch)
        opts {:dir "deploy"}
        arch (case arch
               "amd64" "x86_64"
               "aarch64" "arm64")]
    (proc/shell opts "terraform" "init")
    (proc/shell opts "terraform" "apply" "-var" (str "arch=" arch))))

(defn clean
  []
  (fs/delete-tree "target")
  (fs/delete-if-exists lambda-file)
  (fs/delete-if-exists layer-file))

(comment
  (download "amd64" "1.3.184")

  (latest-version))
