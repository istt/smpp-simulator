import { Component, OnInit, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { LoginModalService, Principal, Account } from 'app/core';

@Component({
    selector: 'jhi-smpp-main',
    templateUrl: './smpp-main.component.html'
})
export class SmppMainComponent implements OnInit {
    account: Account;
    modalRef: NgbModalRef;

    smppMessage: any = {};
    smppCof: any = {};

    @ViewChild('dialogSmppMessage') dialogSmppMessage: NgbModalRef;

    @ViewChild('dialogSmppConfigure') dialogSmppConfigure: NgbModalRef;

    constructor(private principal: Principal, private modalService: NgbModal, private eventManager: JhiEventManager) {}

    ngOnInit() {}

    showDialogSmmMessage(message) {
        this.smppMessage = Object.assign({}, message);
        this.modalService.open(this.dialogSmppMessage);
    }

    showDialogSmppConfigure() {
        this.modalService.open(this.dialogSmppConfigure, { size: 'lg' });
    }
}
