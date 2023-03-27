import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { OwnerService } from './../../service/owner.service';
import { Owner } from '../../dto/owner';

@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.scss'],
})
export class OwnerComponent implements OnInit {
  owners: Owner[] = [];

  constructor(
    private service: OwnerService,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.reloadOwners();
  }

  reloadOwners() {
    this.service.getAll().subscribe({
      next: (data) => {
        this.owners = data;
      },
      error: (error) => {
        console.error('Error fetching owners', error);
        this.errorHandler(error);
      },
    });
  }

  formatOwnerName(name: string | null, numberOfChars: number): string {
    if (name == null || numberOfChars === 0) {
      return '';
    }

    return name.length >= numberOfChars
      ? name.substring(0, numberOfChars) + '...'
      : name;
  }

  private errorHandler(
    error: any,
    action: string | undefined = undefined
  ): void {
    if (error.status === 0) {
      this.notification.error('Backend is not reachable');
    } else if (error.status === 404) {
      this.notification.error(
        `Could not ${action} horse - ` + error.error.error
      );
    } else if (error.status === 409 || error.status === 422) {
      for (const err of error.error.errors) {
        this.notification.error(err);
      }
    } else if (error.status >= 500) {
      this.notification.error(
        error.error.error + ': Something unexpected happend!'
      );
    }
  }
}
