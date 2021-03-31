# Propeller - Project X March 2021

You need to have a k8s cluster running with Istio installed and auto-injection enabled. Please check [here](https://istio.io/latest/docs/setup/getting-started/) for Istio installation details.

## Demo

1. Start the services 

```   
kubectl apply -f kubernetes.yaml
```
    
2. Observe all the services
   
```
kubectl logs -f deployment.apps/plan-meeting
kubectl logs -f deployment.apps/book-flights
kubectl logs -f deployment.apps/book-hotel
kubectl logs -f deployment.apps/book-transport
```
   
3. Send a request to the first service
   
```
curl http://localhost:8080/action
```
   
If not run on Docker Desktop, then change `localhost` to your LoadBalancer IP.

4. Observe services calling each other. Only the last service will fail since Propeller is not setup.

![alt text](markdown/result.png "Result")

5. Now, start Propeller Hazelcast cluster and client by following the steps [here](https://github.com/alparslanavci/propeller-client-server). You should see the `springboot-service` running:  

```
$ kubectl get svc springboot-service
NAME                 TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
springboot-service   ClusterIP   10.4.9.105   <none>        80/TCP    18m
```

6. Install Propeller Envoy filter

```
kubectl apply -f propeller-filter.yaml
```

7. Send a request to the first service again by adding a unique `transactionId` HTTP header:

```
curl -H "transactionId: 465571a0-991e-4a06-aaba-a3575cae1bb5" http://localhost:8080/action
```

8. Observe services calling each other again. Since Propeller is installed this time, all services will call their compensation actions after the last service fails. 