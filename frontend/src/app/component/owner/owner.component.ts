import { Component } from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import { OwnerService } from './../../service/owner.service';
import {Owner} from '../../dto/owner';
import {Observable} from 'rxjs';


@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.scss']
})
export class OwnerComponent {
  owners: Owner[] = [];

  constructor(
    private service: OwnerService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadOwners();
  }
  
  reloadOwners() {
    this.service.getAll()
      .subscribe({
        next: data => {
          this.owners = data;
        },
        error: error => {
          console.error('Error fetching owners', error);
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;

          this.notification.error(errorMessage, 'Could Not Fetch Owners');
        }
      });
  }
}
