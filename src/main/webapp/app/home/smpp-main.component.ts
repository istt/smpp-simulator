import { Component, OnInit, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';
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
    //
    smppMessage: any = {};
    messageState = '';
    smppMessages: any[] = [];
    smppCof: any = {};
    isRandomBulkMessages = false;
    portForPcapParsing = 0;
    pcapFile: any = {};
    isStartSession = false;
    activeModal: NgbActiveModal;

    // Reference to #dialogSmppMessage under view
    @ViewChild('dialogSmppMessage') dialogSmppMessage: NgbModalRef;

    // Reference to #dialogSmppConfigure under view
    @ViewChild('dialogSmppConfigure') dialogSmppConfigure: NgbModalRef;

    // SMPP constants
    listSNpi = ['Unknown', 'ISDN', 'Data', 'Telex', 'Land_Mobile', 'National', 'Private', 'ERMES', 'Internet_IP', 'WAP_Client_Id'];
    listTON = ['Unknown', 'International', 'National', 'Network_Specific', 'Subscriber_Number', 'Alfanumeric', 'Abbreviated'];
    listValidityType = ['NoSpecial', 'ValidityPeriod_5min', 'ValidityPeriod_2hours', 'ScheduleDeliveryTime_5min'];
    listSendingMessageType = ['SubmitSm', 'DataSm', 'DeliverSm', 'SubmitMulti'];
    listMCDeliveryReceipt = ['No', 'onSuccessOrFailure', 'onFailure', 'onSuccess', 'onSuccessTempOrPermanentFailure'];
    listMessagingMode = ['defaultSmscMode', 'datagramm', 'transaction', 'storeAndForward'];

    constructor(
        private principal: Principal,
        private modalService: NgbModal,
        private eventManager: JhiEventManager,
        private http: HttpClient,
        private tracker: JhiTrackerService,
        private alertService: JhiAlertService
    ) {}

    ngOnInit() {
        this.principal.identity().then(account => {
            // Get current configuration
            this.getConfigSmpp();
            // Subscribe for realtime messaging
            this.tracker.subscribe('/topic/smpp-logger');
            this.tracker.receive().subscribe(msg => this.commonGetResult(msg));
        });
    }

    // Display a detail dialog on SMPP event window
    showDialogSmmMessage(message) {
        this.smppMessage = Object.assign({}, message);
        this.activeModal = this.modalService.open(this.dialogSmppMessage);
    }

    // Display the SMPP Configuration Dialog
    showDialogSmppConfigure() {
        this.activeModal = this.modalService.open(this.dialogSmppConfigure, { size: 'lg' });
    }

    commonGetResult = (object: any) => {
        // cF.closeWaitingDialog();
        const result = object.data;
        switch (object.codeStatus) {
            // ADD_MESSAGE = 1;
            case 1:
                this.smppMessages.push(result);
                if (this.smppMessages.length > 10) {
                    this.smppMessages.shift();
                }
                break;
            // REFRESH_STATE = 2;
            case 2:
                this.messageState = result.info;
                break;
            // SESSION_START_FALSE = 3;
            case 3:
                this.smppMessages.push(result);
                if (this.smppMessages.length > 10) {
                    this.smppMessages.shift();
                }
                this.isStartSession = false;
                break;
            // SESSION_START_SUCCESS = 4;
            case 4:
                this.smppMessages.push(result);
                if (this.smppMessages.length > 10) {
                    this.smppMessages.shift();
                }
                this.isStartSession = true;
                break;
            // SESSION_STOP = 5;
            case 5:
                this.smppMessages.push(result);
                if (this.smppMessages.length > 10) {
                    this.smppMessages.shift();
                }
                this.isStartSession = false;
                break;
            // MESSAGE_SUBMIT_FALSE = 6;
            case 6:
                this.smppMessages.push(result);
                if (this.smppMessages.length > 10) {
                    this.smppMessages.shift();
                }
                break;
            default:
                this.smppMessages.push(result);
                if (this.smppMessages.length > 10) {
                    this.smppMessages.shift();
                }
                break;
        }
    };

    // Retrieve the current configuration of SMPP Adapter
    getConfigSmpp = () => {
        console.log('Pulling configuration from server');
        return this.http
            .get(SERVER_API_URL + 'api/smpp-paramaters/get-config', { observe: 'response' })
            .subscribe((res: HttpResponse<any>) => (this.smppCof = res.body), err => this.onError(err));
    };

    // service for update bill
    saveConfigSmpp = () => {
        // var data =  encodeURIComponent(JSON.stringify(object));
        return this.http.post(SERVER_API_URL + 'api/smpp-paramaters/save-config', this.smppCof, { observe: 'response' }).subscribe(
            (res: HttpResponse<any>) => {
                this.smppCof = res.body;
                this.commonGetResult(res.body);
                this.activeModal.close();
            },
            err => this.onError(err)
        );
    };

    // Start SMPP session
    startSmppSession = () => {
        const url = SERVER_API_URL + 'api/smpp-test/start-session';
        return this.http.get(url, { observe: 'response' }).subscribe(res => console.log(res.body), err => this.onError(err));
    };

    stopSmppSession = () => {
        const url = SERVER_API_URL + 'api/smpp-test/stop-session';
        return this.http.get(url, { observe: 'response' }).subscribe(res => console.log(res.body), err => this.onError(err));
    };

    refreshState = () => {
        const url = SERVER_API_URL + 'api/smpp-test/refresh-state';
        return this.http.get(url, { observe: 'response' }).subscribe(res => this.commonGetResult(res.body), err => this.onError(err));
    };

    sendBadPacket = () => {
        const url = SERVER_API_URL + 'api/smpp-test/send-bad-packet';
        return this.http.get(url, { observe: 'response' }).subscribe(res => this.commonGetResult(res.body), err => this.onError(err));
    };

    submitMessage = () => {
        const url = SERVER_API_URL + 'api/smpp-test/submit-message';
        return this.http.get(url, { observe: 'response' }).subscribe(res => console.log(res.body), err => this.onError(err));
    };

    bulkSendingRandom = () => {
        const url = SERVER_API_URL + 'api/smpp-test/bulk-sending-random';
        return this.http.get(url, { observe: 'response' }).subscribe(res => this.commonGetResult(res.body), err => this.onError(err));
    };

    startBulkSending = () => {
        const url = SERVER_API_URL + 'api/smpp-test/bulk-sending-random';
        return this.http.get(url, { observe: 'response' }).subscribe(res => this.commonGetResult(res.body), err => this.onError(err));
    };

    stopBulkSending = () => {
        const url = SERVER_API_URL + 'api/smpp-test/stop-bulk-sending';
        return this.http.get(url, { observe: 'response' }).subscribe(res => this.commonGetResult(res.body), err => this.onError(err));
    };

    onError = err => this.alertService.error(err.error.message, err.message);
}
