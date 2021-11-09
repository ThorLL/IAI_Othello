package main

import (
	DISYS "DISYS/Proto"
	"bufio"
	"context"
	"google.golang.org/grpc"
	"log"
	"os"
	"sync"
	"time"
)

type State int

const (
	HELD     State = 0
	WANTED   State = 1
	RELEASED State = 2
)

func SetupClient() {
	alive()
}

var id int
var connectionString string
var state = RELEASED
var request DISYS.Request

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
	log.Printf("%v state is wanted", id)
	var wg sync.WaitGroup
	wg.Add(len(otherNodes))
	for _, port := range otherNodes {
		go enterRequest(&wg, port, request)
	}
	wg.Wait()
	state = HELD
	log.Printf("%v state is held", id)
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
	log.Printf("%v send request to %v", id, address)
	_, err = c.SendRequest(ctx, request)

	if err != nil {
		log.Fatalf("Error: %v", err)
	}
	wg.Done()
}

func doStuff() {
	log.Printf("%v is doing stuff", id)
	time.Sleep(10 * time.Second)
	state = RELEASED
	log.Printf("%v is done doing stuff, a.k.a. RELEASED", id)
}
