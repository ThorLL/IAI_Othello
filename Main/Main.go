package main

import (
	DISYS "DISYS/Proto"
	"bufio"
	context "context"
	"google.golang.org/grpc"
	"log"
	"net"
	"os"
	"strconv"
	"sync"
	"time"
)

type State int

const (
	HELD     State = 0
	WANTED   State = 1
	RELEASED State = 2
)

var state = RELEASED

var allNodes = []string{"localhost:8001", "localhost:8002", "localhost:8003"}
var otherNodes []string
var id int
var connectionString string
var request DISYS.Request

func main() {
	setup()
}

func setup() {
	readInput()
	setupConnectionString()
	setupOtherNodes()
	go initServer()
	alive()
}

func readInput() {
	if len(os.Args) > 1 {
		id, _ = strconv.Atoi(os.Args[1])
	}
}

func setupConnectionString() {
	connectionString = "localhost:800" + strconv.Itoa(id)
}

func setupOtherNodes() {
	for i := 0; i < len(allNodes); i++ {
		if allNodes[i] != connectionString {
			otherNodes = append(otherNodes, allNodes[i])
		}
	}
}

type server struct {
	DISYS.UnsafeBenchPressQueueServer
}

func alive() {
	log.Printf("Hello I'm %d, my connection string is %v", id, connectionString)
	reader := bufio.NewReader(os.Stdin)
	for {
		line, _, _ := reader.ReadLine()
		if string(line) == "r" {
			request := DISYS.Request{
				Timestamp: time.Now().UTC().String(),
				ProcessID: int32(id),
			}
			multicast(&request)
		}
	}
}

func multicast(request *DISYS.Request) {
	state = WANTED
	log.Println(connectionString, "state is wanted")
	var wg sync.WaitGroup
	wg.Add(len(otherNodes))
	for _, port := range otherNodes {
		go enterRequest(&wg, port, request)
	}
	wg.Wait()
	state = HELD
	log.Println(connectionString, "state is held")
	doStuff()
}

func enterRequest(wg *sync.WaitGroup, address string, request *DISYS.Request) {
	// Set up a connection to the server.
	conn, err := grpc.Dial(address, grpc.WithInsecure(), grpc.WithBlock())
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()
	c := DISYS.NewBenchPressQueueClient(conn)

	ctx, cancel := context.WithTimeout(context.Background(), time.Hour)
	defer cancel()
	log.Println(connectionString, "send request to ", address)
	_, err = c.SendRequest(ctx, request)

	if err != nil {
		log.Fatalf("Error: %v", err)
	}
	wg.Done()
}

func (s server) SendRequest(ctx context.Context, request *DISYS.Request) (*DISYS.Response, error) {
	for {
		if state == HELD || (state == WANTED && ownRequestLower(request)) {
			continue
		} else {
			break
		}
	}
	response := DISYS.Response{}
	log.Println(connectionString, "Server sending response back - AKA done")
	return &response, nil
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
func doStuff() {
	log.Println(connectionString, "is doing stuff")
	time.Sleep(10 * time.Second)
	state = RELEASED
	log.Println(connectionString, "is done doing stuff, a.k.a. RELEASED")
}

func ownRequestLower(otherRequest *DISYS.Request) (iAmLower bool) {
	otherTime, _ := time.Parse(time.Layout, request.Timestamp)
	myTime, _ := time.Parse(time.Layout, request.Timestamp)

	if myTime.Equal(otherTime) {
		return request.ProcessID < otherRequest.ProcessID
	}
	return myTime.Before(otherTime)
}
