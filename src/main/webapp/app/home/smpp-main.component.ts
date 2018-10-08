import { Component, OnInit, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { createRequestOption } from 'app/shared';
import { SERVER_API_URL } from 'app/app.constants';
import { LoginModalService, Principal, Account, JhiTrackerService } from 'app/core';
import { SmppConfigService } from './smpp-config.service';

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

    // Configure the Message Submission Templates
    @ViewChild('dialogSubmitCfg') dialogSubmitCfg: NgbModalRef;

    // Reference to #dialogSmppConfigure under view
    @ViewChild('dialogSmppConfigure') dialogSmppConfigure: NgbModalRef;

    // SMPP constants
    listSNpi = ['Unknown', 'ISDN', 'Data', 'Telex', 'Land_Mobile', 'National', 'Private', 'ERMES', 'Internet_IP', 'WAP_Client_Id'];
    listTON = ['Unknown', 'International', 'National', 'Network_Specific', 'Subscriber_Number', 'Alfanumeric', 'Abbreviated'];
    listValidityType = ['NoSpecial', 'ValidityPeriod_5min', 'ValidityPeriod_2hours', 'ScheduleDeliveryTime_5min'];
    listSendingMessageType = ['SubmitSm', 'DataSm', 'DeliverSm', 'SubmitMulti'];
    listMCDeliveryReceipt = ['No', 'onSuccessOrFailure', 'onFailure', 'onSuccess', 'onSuccessTempOrPermanentFailure'];
    listMessagingMode = ['defaultSmscMode', 'datagramm', 'transaction', 'storeAndForward'];
    listTlv = [
        { tagName: 'TAG_SOURCE_TELEMATICS_ID', tag: '0x0010' },
        { tagName: 'TAG_PAYLOAD_TYPE', tag: '0x0019' },
        { tagName: 'TAG_PRIVACY_INDICATOR', tag: '0x0201' },
        { tagName: 'TAG_USER_MESSAGE_REFERENCE', tag: '0x0204' },
        { tagName: 'TAG_USER_RESPONSE_CODE', tag: '0x0205' },
        { tagName: 'TAG_SOURCE_PORT', tag: '0x020A' },
        { tagName: 'TAG_DESTINATION_PORT', tag: '0x020B' },
        { tagName: 'TAG_SAR_MSG_REF_NUM', tag: '0x020C' },
        { tagName: 'TAG_LANGUAGE_INDICATOR', tag: '0x020D' },
        { tagName: 'TAG_SAR_TOTAL_SEGMENTS', tag: '0x020E' },
        { tagName: 'TAG_SAR_SEGMENT_SEQNUM', tag: '0x020F' },
        { tagName: 'TAG_SOURCE_SUBADDRESS', tag: '0x0202' },
        { tagName: 'TAG_DEST_SUBADDRESS', tag: '0x0203' },
        { tagName: 'TAG_CALLBACK_NUM', tag: '0x0381' },
        { tagName: 'TAG_MESSAGE_PAYLOAD', tag: '0x0424' },
        { tagName: 'TAG_SC_INTERFACE_VERSION', tag: '0x0210' },
        { tagName: 'TAG_DISPLAY_TIME', tag: '0x1201' },
        { tagName: 'TAG_MS_VALIDITY', tag: '0x1204' },
        { tagName: 'TAG_DPF_RESULT', tag: '0x0420' },
        { tagName: 'TAG_SET_DPF', tag: '0x0421' },
        { tagName: 'TAG_MS_AVAIL_STATUS', tag: '0x0422' },
        { tagName: 'TAG_NETWORK_ERROR_CODE', tag: '0x0423' },
        { tagName: 'TAG_DELIVERY_FAILURE_REASON', tag: '0x0425' },
        { tagName: 'TAG_MORE_MSGS_TO_FOLLOW', tag: '0x0426' },
        { tagName: 'TAG_MSG_STATE', tag: '0x0427' },
        { tagName: 'TAG_CONGESTION_STATE', tag: '0x0428' },
        { tagName: 'TAG_CALLBACK_NUM_PRES_IND', tag: '0x0302' },
        { tagName: 'TAG_CALLBACK_NUM_ATAG', tag: '0x0303' },
        { tagName: 'TAG_NUM_MSGS', tag: '0x0304' },
        { tagName: 'TAG_SMS_SIGNAL', tag: '0x1203' },
        { tagName: 'TAG_ALERT_ON_MSG_DELIVERY', tag: '0x130C' },
        { tagName: 'TAG_ITS_REPLY_TYPE', tag: '0x1380' },
        { tagName: 'TAG_ITS_SESSION_INFO', tag: '0x1383' },
        { tagName: 'TAG_USSD_SERVICE_OP', tag: '0x0501' },
        { tagName: 'TAG_BROADCAST_CHANNEL_INDICATOR', tag: '0x0600' },
        { tagName: 'TAG_BROADCAST_CONTENT_TYPE', tag: '0x0601' },
        { tagName: 'TAG_BROADCAST_CONTENT_TYPE_INFO', tag: '0x0602' },
        { tagName: 'TAG_BROADCAST_MESSAGE_CLASS', tag: '0x0603' },
        { tagName: 'TAG_BROADCAST_REP_NUM', tag: '0x0604' },
        { tagName: 'TAG_BROADCAST_FREQUENCY_INTERVAL', tag: '0x0605' },
        { tagName: 'TAG_BROADCAST_AREA_IDENTIFIER', tag: '0x0606' },
        { tagName: 'TAG_BROADCAST_ERROR_STATUS', tag: '0x0607' },
        { tagName: 'TAG_BROADCAST_AREA_SUCCESS', tag: '0x0608' },
        { tagName: 'TAG_BROADCAST_END_TIME', tag: '0x0609' },
        { tagName: 'TAG_BROADCAST_SERVICE_GROUP', tag: '0x060A' },
        { tagName: 'TAG_SOURCE_NETWORK_ID', tag: '0x060D' },
        { tagName: 'TAG_DEST_NETWORK_ID', tag: '0x060E' },
        { tagName: 'TAG_SOURCE_NODE_ID', tag: '0x060F' },
        { tagName: 'TAG_DEST_NODE_ID', tag: '0x0610' },
        { tagName: 'TAG_BILLING_IDENTIFICATION', tag: '0x060B' },
        { tagName: 'TAG_ORIG_MSC_ADDR', tag: '0x8081' },
        { tagName: 'TAG_DEST_MSC_ADDR', tag: '0x8082' },
        { tagName: 'TAG_DEST_ADDR_SUBUNIT', tag: '0x0005' },
        { tagName: 'TAG_DEST_NETWORK_TYPE', tag: '0x0006' },
        { tagName: 'TAG_DEST_BEAR_TYPE', tag: '0x0007' },
        { tagName: 'TAG_DEST_TELE_ID', tag: '0x0008' },
        { tagName: 'TAG_SOURCE_ADDR_SUBUNIT', tag: '0x000D' },
        { tagName: 'TAG_SOURCE_NETWORK_TYPE', tag: '0x000E' },
        { tagName: 'TAG_SOURCE_BEAR_TYPE', tag: '0x000F' },
        { tagName: 'TAG_SOURCE_TELE_ID', tag: '0x0010' },
        { tagName: 'TAG_QOS_TIME_TO_LIVE', tag: '0x0017' },
        { tagName: 'TAG_ADD_STATUS_INFO', tag: '0x001D' },
        { tagName: 'TAG_RECEIPTED_MSG_ID', tag: '0x001E' },
        { tagName: 'TAG_MS_MSG_WAIT_FACILITIES', tag: '0x0030' }
    ];

    constructor(
        public smppConfig: SmppConfigService,
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

    // Display the SMPP packet configuration
    showDialogSubmitCfg() {
        this.activeModal = this.modalService.open(this.dialogSubmitCfg, { size: 'lg' });
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
            .subscribe((res: HttpResponse<any>) => Object.assign(this.smppCof, res.body), err => this.onError(err));
    };

    // service for update bill
    saveConfigSmpp = () => {
        // var data =  encodeURIComponent(JSON.stringify(object));
        return this.http.post(SERVER_API_URL + 'api/smpp-paramaters/save-config', this.smppCof, { observe: 'response' }).subscribe(
            (res: HttpResponse<any>) => {
                Object.assign(this.smppCof, res.body);
                // this.commonGetResult(res.body);
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

    addTlv() {
        if (!this.smppCof || !this.smppCof.tlvList) {
            this.smppCof.tlvList = [];
        }
        this.smppCof.tlvList.push({ tag: '', value: '' });
    }

    removeTlv(tlv) {
        this.smppCof.tlvList = this.smppCof.tlvList.filter(i => i !== tlv);
    }
}
