package main

import (
	DISYS "DISYS/Proto"
	"google.golang.org/grpc"
	"log"
	"net"
	"os"
	"strconv"
)

type server struct {
	DISYS.UnsafeBenchPressQueueServer
}

var allNodes = []string{"localhost:8001", "localhost:8002", "localhost:8003"}
var otherNodes []string

func setupServer() {
	readArguments()
	createClientConnectionString()
	setupOtherNodes()
	go initServer()
}

func readArguments() {
	if len(os.Args) > 1 {
		id, _ = strconv.Atoi(os.Args[1])
	}
}

func createClientConnectionString() {
	connectionString = "localhost:800" + strconv.Itoa(id)
}

func setupOtherNodes() {
	for i := 0; i < len(allNodes); i++ {
		if allNodes[i] != connectionString {
			otherNodes = append(otherNodes, allNodes[i])
		}
	}
}
func initServer() {
	lis, err := net.Listen("tcp", connectionString)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()
	DISYS.RegisterBenchPressQueueServer(s, &server{})
	log.Printf("server listening at %v", lis.Addr())
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
