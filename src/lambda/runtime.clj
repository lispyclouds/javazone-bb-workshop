(ns lambda.runtime
  "Integration with the AWS Lambda runtime API,
  see https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html"
  (:require [babashka.http-client :as http]
            [cheshire.core :as json]))

(defn- get-lambda-invocation-request [runtime-api]
  (http/get (str "http://" runtime-api "/2018-06-01/runtime/invocation/next") {:timeout 900000}))

(defn- send-response [runtime-api lambda-runtime-aws-request-id response-body]
  (http/post (str "http://" runtime-api "/2018-06-01/runtime/invocation/" lambda-runtime-aws-request-id "/response")
             {:body response-body :headers {"Content-Type" "application/json"}}))

(defn- send-error [runtime-api lambda-runtime-aws-request-id error-body]
  (http/post (str "http://" runtime-api "/2018-06-01/runtime/invocation/" lambda-runtime-aws-request-id "/error")
             {:body error-body :headers {"Content-Type" "application/json"}}))

(defn- request->response [request-body handler-fn]
  (let [decoded-request (json/decode request-body true)]
    (if-let [body (:body decoded-request)]
      (json/encode
       {:statusCode 200
        :body (json/encode (handler-fn (json/decode body true)))})
      (json/encode (handler-fn decoded-request)))))

(defn init [handler-fn]
  (let [runtime-api (System/getenv "AWS_LAMBDA_RUNTIME_API")]
    (loop [req (get-lambda-invocation-request runtime-api)]
      (let [lambda-runtime-aws-request-id (-> req :headers (get "lambda-runtime-aws-request-id"))]
        (when-let [error (get req :error)]
          (send-error runtime-api lambda-runtime-aws-request-id (str error))
          (throw (Exception. (str error))))
        (try
          (send-response runtime-api
                         lambda-runtime-aws-request-id
                         (request->response (get req :body) handler-fn))
          (catch Exception e
            (send-error runtime-api lambda-runtime-aws-request-id (str e)))))
      (recur (get-lambda-invocation-request runtime-api)))))

(comment
  (http/get "https://postman-echo.com/get"))
