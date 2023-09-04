(ns lambda.runtime
  (:require [babashka.http-client :as http]
            [cheshire.core :as json]))

;; BAD things happen if you timeout
(def massive-timeout-ms (* 1000 60 60 24))

(defn- next-invocation-request
  [url]
  (http/get (str url "/next") {:timeout massive-timeout-ms}))

(defn- send-response
  [url request-id response-body]
  (http/post (str url "/" request-id "/response")
             {:body response-body
              :headers {"Content-Type" "application/json"}}))

(defn- send-error
  [url request-id error-body]
  (http/post (str url "/" request-id "/error")
             {:body error-body
              :headers {"Content-Type" "application/json"}}))

(defn- request->response
  [headers request-body handler-fn]
  (let [decoded-request (json/parse-string request-body true)]
    (if-let [body (:body decoded-request)]
      (json/generate-string
       {:statusCode 200
        :body (-> body
                  (json/parse-string true)
                  (handler-fn headers)
                  (json/generate-string))})
      (json/generate-string (handler-fn decoded-request headers)))))

(defn init
  [handler-fn]
  (let [url (str "http://" (System/getenv "AWS_LAMBDA_RUNTIME_API") "/2018-06-01/runtime/invocation")]
    (loop [{:keys [headers body error]} (next-invocation-request url)]
      (let [request-id (get headers "lambda-runtime-aws-request-id")]
        (when error
          (send-error url request-id (str error))
          (throw (Exception. (str error))))
        (try
          (send-response url request-id (request->response headers body handler-fn))
          (catch Exception e
            (send-error url request-id (str e)))))
      (recur (next-invocation-request url)))))

(comment
  (http/get "https://postman-echo.com/get"))
