(ns lambda.runtime
  (:require [babashka.http-client :as http]
            [cheshire.core :as json]))

;; BAD things happen if you timeout
(def massive-timeout-ms (* 1000 60 60 24))

(defn- next-invocation-request
  [url]
  (http/get (str url "/next") {:timeout massive-timeout-ms}))

(defn- send-response
  [url lambda-runtime-aws-request-id response-body]
  (http/post (str url "/" lambda-runtime-aws-request-id "/response")
             {:body response-body
              :headers {"Content-Type" "application/json"}}))

(defn- send-error
  [url lambda-runtime-aws-request-id error-body]
  (http/post (str url "/" lambda-runtime-aws-request-id "/error")
             {:body error-body
              :headers {"Content-Type" "application/json"}}))

(defn- request->response
  [request-body handler-fn]
  (let [decoded-request (json/decode request-body true)]
    (if-let [body (:body decoded-request)]
      (json/encode
       {:statusCode 200
        :body (json/encode (handler-fn (json/decode body true)))})
      (json/encode (handler-fn decoded-request)))))

(defn init
  [handler-fn]
  (let [url (str "http://" (System/getenv "AWS_LAMBDA_RUNTIME_API") "/2018-06-01/runtime/invocation")]
    (loop [req (next-invocation-request url)]
      (let [lambda-runtime-aws-request-id (-> req :headers (get "lambda-runtime-aws-request-id"))]
        (when-let [error (get req :error)]
          (send-error url lambda-runtime-aws-request-id (str error))
          (throw (Exception. (str error))))
        (try
          (send-response url
                         lambda-runtime-aws-request-id
                         (request->response (get req :body) handler-fn))
          (catch Exception e
            (send-error url lambda-runtime-aws-request-id (str e)))))
      (recur (next-invocation-request url)))))

(comment
  (http/get "https://postman-echo.com/get"))
