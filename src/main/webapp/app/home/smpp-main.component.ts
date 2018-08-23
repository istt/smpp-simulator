import { Component, OnInit, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { createRequestOption } from 'app/shared';
import { SERVER_API_URL } from 'app/app.constants';
import { LoginModalService, Principal, Account, JhiTrackerService } from 'app/core';

@Component({
    selector: 'jhi-smpp-main',
    templateUrl: './smpp-main.component.html'
})
export class SmppMainComponent implements OnInit {
    account: Account;
    modalRef: NgbModalRef;

    smppMessage: any = {};
    messageState: any = {};
    smppMessages: any[] = [];
    smppCof: any = {};
    isRandomBulkMessages = false;
    portForPcapParsing = 0;
    pcapFile: any = {};
    isStartSession = false;

    // Reference to #dialogSmppMessage under view
    @ViewChild('dialogSmppMessage') dialogSmppMessage: NgbModalRef;

    // Reference to #dialogSmppConfigure under view
    @ViewChild('dialogSmppConfigure') dialogSmppConfigure: NgbModalRef;

    constructor(
        private principal: Principal,
        private modalService: NgbModal,
        private eventManager: JhiEventManager,
        private http: HttpClient,
        private tracker: JhiTrackerService
    ) {}

    ngOnInit() {
        this.principal.identity().then(account => {
            // Get current configuration
            this.getConfigSmpp();
            // Subscribe for realtime messaging
            this.tracker.subscribe('/topic/smpp-logger');
            this.tracker.receive().subscribe(msg => (this.messageState = Object.assign({}, msg.data)));
        });
    }

    // Display a detail dialog on SMPP event window
    showDialogSmmMessage(message) {
        this.smppMessage = Object.assign({}, message);
        this.modalService.open(this.dialogSmppMessage);
    }

    // Display the SMPP Configuration Dialog
    showDialogSmppConfigure() {
        this.modalService.open(this.dialogSmppConfigure, { size: 'lg' });
    }

    query(req: any): Observable<HttpResponse<any>> {
        const params: HttpParams = createRequestOption(req);
        params.set('fromDate', req.fromDate);
        params.set('toDate', req.toDate);
        const requestURL = SERVER_API_URL + 'api/management/audits';
        return this.http.get(requestURL, {
            params,
            observe: 'response'
        });
    }

    // Retrieve the current configuration of SMPP Adapter
    getConfigSmpp = () => {
        console.log('Pulling configuration from server');
        return this.http
            .get(SERVER_API_URL + 'api/smpp-paramaters/get-config', { observe: 'response' })
            .subscribe((res: HttpResponse<any>) => (this.smppCof = res.body));
    };

    // service for update bill
    saveConfigSmpp = () => {
        // var data =  encodeURIComponent(JSON.stringify(object));
        return this.http
            .post(SERVER_API_URL + 'api/smpp-paramaters/save-config', this.smppCof, { observe: 'response' })
            .subscribe((res: HttpResponse<any>) => (this.smppCof = res.body));
    };

    // Start SMPP session
    startSmppSession = () => {
        const url = SERVER_API_URL + 'api/smpp-test/start-session';
        return this.http.get(url, { observe: 'response' }).subscribe(res => (this.isStartSession = true));
    };

    stopSmppSession = () => {
        const url = SERVER_API_URL + 'api/smpp-test/stop-session';
        return this.http.get(url, { observe: 'response' }).subscribe(res => (this.isStartSession = false));
    };

    refreshState = () => {
        const url = SERVER_API_URL + 'api/smpp-test/refresh-state';
        return this.http.get(url, { observe: 'response' });
    };

    sendBadPacket = () => {
        const url = SERVER_API_URL + 'api/smpp-test/send-bad-packet';
        return this.http.get(url, { observe: 'response' });
    };

    submitMessage = () => {
        const url = SERVER_API_URL + 'api/smpp-test/submit-message';
        return this.http.get(url, { observe: 'response' });
    };

    bulkSendingRandom = () => {
        const url = SERVER_API_URL + 'api/smpp-test/bulk-sending-random';
        return this.http.get(url, { observe: 'response' });
    };

    startBulkSending = () => {
        const url = SERVER_API_URL + 'api/smpp-test/bulk-sending-random';
        return this.http.get(url, { observe: 'response' });
    };

    stopBulkSending = () => {
        const url = SERVER_API_URL + 'api/smpp-test/stop-bulk-sending';
        return this.http.get(url, { observe: 'response' });
    };
}
